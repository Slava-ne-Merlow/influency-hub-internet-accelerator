package de.vyacheslav.kushchenko.staff.vpn.data.user.model

import de.vyacheslav.kushchenko.staff.vpn.api.model.UserDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.ManualAccessType as ManualAccessTypeDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.Role as RoleDto
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.ManualAccessType
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.UserRole
import jakarta.persistence.Column
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.OffsetDateTime
import java.util.*

data class User(
    val id: UUID? = null,
    val telegramId: Long,
    val firstName: String,
    val lastName: String? = null,
    val tgUsername: String? = null,
    val avatarUrl: String? = null,
    val role: UserRole,
    val manualAccessType: ManualAccessType = ManualAccessType.NONE,
    val manualAccessUntil: OffsetDateTime? = null,
    @get:JvmName("getPassword0")
    val password: String? = null,
) : UserDetails {

    override fun getAuthorities() = mutableSetOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String? = password

    override fun getUsername(): String = telegramId.toString()

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    fun hasActiveManualAccess(now: OffsetDateTime = OffsetDateTime.now()): Boolean =
        when (manualAccessType) {
            ManualAccessType.NONE -> false
            ManualAccessType.THREE_MONTHS -> manualAccessUntil?.isAfter(now) == true
            ManualAccessType.FOREVER -> true
        }

}

fun User.toDto() = UserDto(
    id = this.id!!,
    telegramId = telegramId,
    firstName = firstName,
    lastName = lastName,
    username = tgUsername,
    avatarUrl = avatarUrl,
    manualAccessType = ManualAccessTypeDto.valueOf(manualAccessType.name),
    manualAccessUntil = manualAccessUntil,
    role = RoleDto.valueOf(this.role.name)
)
