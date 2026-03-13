package de.vyacheslav.kushchenko.staff.vpn.service

import de.vyacheslav.kushchenko.staff.vpn.api.model.AuthResponse
import de.vyacheslav.kushchenko.staff.vpn.data.user.dao.UserEntity.Companion.asEntity
import de.vyacheslav.kushchenko.staff.vpn.data.user.dao.UserEntity.Companion.asModel
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.ManualAccessType
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.UserRole
import de.vyacheslav.kushchenko.staff.vpn.data.user.model.User
import de.vyacheslav.kushchenko.staff.vpn.data.user.model.toDto
import de.vyacheslav.kushchenko.staff.vpn.data.user.repository.UserRepository
import de.vyacheslav.kushchenko.staff.vpn.util.PasswordGenerator
import de.vyacheslav.kushchenko.staff.vpn.web.exception.base.ConflictException
import de.vyacheslav.kushchenko.staff.vpn.web.exception.base.InvalidBodyException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class AuthenticationService(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val objectMapper: com.fasterxml.jackson.databind.ObjectMapper,
    @Value("\${app.owner.tgids}") private val ownerIdsRaw: String,
    @Value("\${app.admin.tgids}") private val adminIdsRaw: String,
    @Value("\${TELEGRAM_BOT_TOKEN:\${TELEGRAM_TOKEN:}}") private val telegramToken: String
) {
    private val logger = LoggerFactory.getLogger(AuthenticationService::class.java)

    private val ownerIds: Set<Long> by lazy {
        ownerIdsRaw
            .split(",")
            .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    private val adminIds: Set<Long> by lazy {
        adminIdsRaw
            .split(",")
            .mapNotNull { it.trim().takeIf(String::isNotEmpty) }
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }



    fun register(
        telegramId: Long,
        firstName: String,
        lastName: String? = null,
        username: String? = null,
        avatarUrl: String? = null,
        rawPassword: String? = null,
    ): User {
        if (userService.existsByTelegramId(telegramId)) {
            throw ConflictException("User with this telegramId already exists")
        }

        val passwordHash = passwordEncoder.encode(rawPassword ?: PasswordGenerator.generate())

        val user = User(
            telegramId = telegramId,
            firstName = firstName,
            lastName = lastName,
            tgUsername = username,
            avatarUrl = avatarUrl,
            password = passwordHash,
            role = when {
                ownerIds.contains(telegramId) -> UserRole.OWNER
                adminIds.contains(telegramId) -> UserRole.ADMIN
                else -> UserRole.USER
            },
            manualAccessType = ManualAccessType.NONE,
            manualAccessUntil = null,

            )

        return userRepository.save(user.asEntity()).asModel()
    }

    fun upsertTelegramUser(
        telegramId: Long,
        firstName: String,
        lastName: String?,
        username: String? = null,
        avatarUrl: String? = null,
    ): User {
        val existing = userRepository.findByTelegramId(telegramId)
        if (existing != null) {
        val updated = existing.copy(
            firstName = firstName,
            lastName = lastName,
            tgUsername = username ?: existing.tgUsername,
            avatarUrl = avatarUrl ?: existing.avatarUrl,
            manualAccessType = existing.manualAccessType,
            manualAccessUntil = existing.manualAccessUntil,
        )

            return userRepository.save(updated).asModel()
        }

        return register(
            telegramId = telegramId,
            firstName = firstName,
            lastName = lastName,
            username = username,
            avatarUrl = avatarUrl,
            rawPassword = null,
        )
    }

    fun telegramAuth(initData: String): AuthResponse {
        if (telegramToken.isBlank()) {
            throw IllegalStateException("Telegram token is not configured")
        }

        val data = parseInitData(initData)
        val hash = data.remove("hash") ?: throw InvalidBodyException("initData missing hash")

        val dataCheckString = data.entries
            .sortedBy { it.key }
            .joinToString("\n") { "${it.key}=${it.value}" }

        val hmac = Mac.getInstance("HmacSHA256")
        hmac.init(SecretKeySpec("WebAppData".toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val secretKey = hmac.doFinal(telegramToken.toByteArray(StandardCharsets.UTF_8))

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey, "HmacSHA256"))
        val calculatedHash = mac.doFinal(dataCheckString.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

        if (!calculatedHash.equals(hash, ignoreCase = true)) {
            throw BadCredentialsException("Invalid Telegram signature")
        }

        val userJson = data["user"] ?: throw InvalidBodyException("initData missing user")
        val telegramUser = parseTelegramUser(userJson)

        val user = upsertTelegramUser(
            telegramId = telegramUser.id,
            firstName = telegramUser.firstName,
            lastName = telegramUser.lastName,
            username = telegramUser.username,
            avatarUrl = telegramUser.avatarUrl,
        )

        val accessToken = jwtService.generateAccessToken(user.id!!)

        logger.info("Telegram auth ok for telegramId={}", telegramUser.id)

        return AuthResponse(
            accessToken = accessToken,
            user = user.toDto()
        )
    }

    private fun parseInitData(initData: String): MutableMap<String, String> {
        val decoded = URLDecoder.decode(initData, StandardCharsets.UTF_8)
        return decoded.split("&")
            .mapNotNull { pair ->
                val idx = pair.indexOf("=")
                if (idx <= 0) return@mapNotNull null
                val key = pair.substring(0, idx)
                val value = pair.substring(idx + 1)
                key to value
            }
            .toMap(mutableMapOf())
    }

    private data class TelegramUser(
        val id: Long,
        val firstName: String,
        val lastName: String?,
        val username: String?,
        val languageCode: String?,
        val avatarUrl: String?,
    )

    private fun parseTelegramUser(json: String): TelegramUser {
        // Telegram sends JSON in 'user' field
        val node = objectMapper.readTree(json)
        return TelegramUser(
            id = node.get("id").asLong(),
            firstName = node.get("first_name").asText(),
            lastName = node.get("last_name")?.asText(),
            username = node.get("username")?.asText(),
            languageCode = node.get("language_code")?.asText(),
            avatarUrl = node.get("photo_url")?.asText(),
        )
    }

}
