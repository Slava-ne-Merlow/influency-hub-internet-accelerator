package de.vyacheslav.kushchenko.staff.vpn.data.chat.repository

import de.vyacheslav.kushchenko.staff.vpn.data.chat.dao.ChatEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChatRepository : JpaRepository<ChatEntity, UUID> {
    fun findByChatId(chatId: Long): ChatEntity?
    fun findAllByStatus(status: de.vyacheslav.kushchenko.staff.vpn.data.chat.enum.ChatStatus): List<ChatEntity>
}
