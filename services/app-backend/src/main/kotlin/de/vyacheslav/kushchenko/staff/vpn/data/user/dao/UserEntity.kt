package de.vyacheslav.kushchenko.staff.vpn.data.user.dao

import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.ManualAccessType
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.UserRole
import de.vyacheslav.kushchenko.staff.vpn.data.user.model.User
import de.vyacheslav.kushchenko.staff.vpn.util.model.EntityConverter
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "users")
data class UserEntity(

    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "telegram_id", unique = true)
    val telegramId: Long,

    val firstName: String,

    @Column(nullable = true)
    val lastName: String? = null,

    @Column(name = "username", nullable = true)
    val tgUsername: String? = null,

    @Column(name = "avatar_url", nullable = true)
    val avatarUrl: String? = null,

    @Enumerated(EnumType.STRING)
    val role: UserRole,

    @Enumerated(EnumType.STRING)
    @Column(name = "manual_access_type")
    val manualAccessType: ManualAccessType = ManualAccessType.NONE,

    @Column(name = "manual_access_until", nullable = true)
    val manualAccessUntil: OffsetDateTime? = null,

    @Column(nullable = true)
    val password: String?,

) {

    companion object : EntityConverter<User, UserEntity> {
        override fun UserEntity.asModel() = User(
            id = id,
            telegramId = telegramId,
            firstName = firstName,
            lastName = lastName,
            tgUsername = tgUsername,
            avatarUrl = avatarUrl,
            role = role,
            manualAccessType = manualAccessType,
            manualAccessUntil = manualAccessUntil,
            password = password
        )

        override fun User.asEntity() = UserEntity(
            id = id,
            telegramId = telegramId,
            firstName = firstName,
            lastName = lastName,
            tgUsername = tgUsername,
            avatarUrl = avatarUrl,
            role = role,
            manualAccessType = manualAccessType,
            manualAccessUntil = manualAccessUntil,
            password = password
        )
    }

}
