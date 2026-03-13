package de.vyacheslav.kushchenko.staff.vpn.web.controller

import de.vyacheslav.kushchenko.staff.vpn.api.UsersApi
import de.vyacheslav.kushchenko.staff.vpn.api.model.GrantManualAccessRequest
import de.vyacheslav.kushchenko.staff.vpn.api.model.UserDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.UserUpdateRequest
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.ManualAccessType
import de.vyacheslav.kushchenko.staff.vpn.data.user.model.toDto
import de.vyacheslav.kushchenko.staff.vpn.service.TelegramNotificationService
import de.vyacheslav.kushchenko.staff.vpn.service.UserService
import de.vyacheslav.kushchenko.staff.vpn.util.getRequestUser
import de.vyacheslav.kushchenko.staff.vpn.util.ok
import de.vyacheslav.kushchenko.staff.vpn.web.security.annotation.Authorized
import de.vyacheslav.kushchenko.staff.vpn.web.security.annotation.IsAdmin
import de.vyacheslav.kushchenko.staff.vpn.web.exception.base.InvalidBodyException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
class UserController(
    private val userService: UserService,
    private val telegramNotificationService: TelegramNotificationService,
) : UsersApi {
    @Authorized
    override fun getMe(): ResponseEntity<UserDto> {
        val logger = LoggerFactory.getLogger(UserController::class.java)
        val user = getRequestUser().toDto().ok()
        logger.warn(user.toString())

        return getRequestUser().toDto().ok()
    }

    @Authorized
    override fun updateMe(request: UserUpdateRequest): ResponseEntity<UserDto> =
        userService.update(getRequestUser().id!!, request).toDto().ok()

    @IsAdmin
    override fun getUserById(id: UUID): ResponseEntity<UserDto> =
        userService.getById(id).toDto().ok()

    @IsAdmin
    override fun grantUserManualAccess(id: UUID, grantManualAccessRequest: GrantManualAccessRequest): ResponseEntity<UserDto> {
        val accessType = ManualAccessType.valueOf(grantManualAccessRequest.accessType.name)
        if (accessType == ManualAccessType.NONE) {
            throw InvalidBodyException("Manual access type NONE cannot be granted")
        }

        val accessUntil = when (accessType) {
            ManualAccessType.THREE_MONTHS -> OffsetDateTime.now(ZoneOffset.UTC).plusMonths(3)
            ManualAccessType.FOREVER -> null
            ManualAccessType.NONE -> null
        }
        val updatedUser = userService.grantManualAccess(id, accessType, accessUntil)
        telegramNotificationService.notifyManualAccessGranted(updatedUser, accessType, accessUntil)
        return updatedUser.toDto().ok()
    }

    @IsAdmin
    override fun revokeUserManualAccess(id: UUID): ResponseEntity<UserDto> {
        val updatedUser = userService.revokeManualAccess(id)
        telegramNotificationService.notifyManualAccessRevoked(updatedUser)
        return updatedUser.toDto().ok()
    }

    @IsAdmin
    override fun getUsers(ids: List<UUID>?): ResponseEntity<List<UserDto>> {
        if (ids.isNullOrEmpty()) {
            return userService.getAll().map { it.toDto() }.ok()
        }
        return userService.getByIds(ids).map { it.toDto() }.ok()
    }

}

