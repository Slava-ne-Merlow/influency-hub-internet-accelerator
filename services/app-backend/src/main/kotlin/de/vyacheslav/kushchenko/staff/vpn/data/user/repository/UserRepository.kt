package de.vyacheslav.kushchenko.staff.vpn.data.user.repository

import de.vyacheslav.kushchenko.staff.vpn.data.user.dao.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.*

@RepositoryRestResource(exported = false)
interface UserRepository : JpaRepository<UserEntity, UUID> {

    fun findByTelegramId(telegramId: Long): UserEntity?

    fun existsByTelegramId(telegramId: Long): Boolean

}
