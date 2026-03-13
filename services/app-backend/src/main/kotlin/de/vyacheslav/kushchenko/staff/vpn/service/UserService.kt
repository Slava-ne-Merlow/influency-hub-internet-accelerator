package de.vyacheslav.kushchenko.staff.vpn.service

import de.vyacheslav.kushchenko.staff.vpn.api.model.UserUpdateRequest
import de.vyacheslav.kushchenko.staff.vpn.data.user.dao.UserEntity.Companion.asEntity
import de.vyacheslav.kushchenko.staff.vpn.data.user.dao.UserEntity.Companion.asModel
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.ManualAccessType
import de.vyacheslav.kushchenko.staff.vpn.data.user.model.User
import de.vyacheslav.kushchenko.staff.vpn.data.user.repository.UserRepository
import de.vyacheslav.kushchenko.staff.vpn.web.exception.base.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    fun getAll() = userRepository.findAll().map { it.asModel() }

    fun getById(id: UUID): User {

        val user = userRepository.findById(id).orElseThrow { NotFoundException("User not found") }

        return user.asModel()
    }

    fun getByIdOrNull(id: UUID): User? =
        userRepository.findById(id).orElse(null)?.asModel()

    fun getByTelegramId(telegramId: Long): User {
        val user = userRepository.findByTelegramId(telegramId) ?: throw NotFoundException("User not found")

        return user.asModel()
    }

    fun existsByTelegramId(telegramId: Long) = userRepository.existsByTelegramId(telegramId)

    fun getByTelegramIdOrNull(telegramId: Long): User? = userRepository.findByTelegramId(telegramId)?.asModel()

    fun getByIds(ids: List<UUID>): List<User> =
        userRepository.findAllById(ids).map { it.asModel() }

    fun grantManualAccess(userId: UUID, accessType: ManualAccessType, accessUntil: OffsetDateTime?): User {
        val user = getById(userId)
        val updated = user.copy(
            manualAccessType = accessType,
            manualAccessUntil = accessUntil
        )
        userRepository.save(updated.asEntity())
        return updated
    }

    fun revokeManualAccess(userId: UUID): User {
        val user = getById(userId)
        val updated = user.copy(
            manualAccessType = ManualAccessType.NONE,
            manualAccessUntil = null
        )
        userRepository.save(updated.asEntity())
        return updated
    }

    fun update(userId: UUID, request: UserUpdateRequest): User {
        val user = getById(userId)
        val newUser = user.copy(firstName = request.name)
        userRepository.save(newUser.asEntity())

        return newUser
    }

    @Transactional
    fun delete(userId: UUID) {
        getById(userId)
        userRepository.deleteById(userId)
    }

}
