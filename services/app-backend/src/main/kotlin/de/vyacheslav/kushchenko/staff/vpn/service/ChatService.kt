package de.vyacheslav.kushchenko.staff.vpn.service

import de.vyacheslav.kushchenko.staff.vpn.data.chat.dao.ChatEntity.Companion.asEntity
import de.vyacheslav.kushchenko.staff.vpn.data.chat.dao.ChatEntity.Companion.asModel
import de.vyacheslav.kushchenko.staff.vpn.data.chat.enum.ChatStatus
import de.vyacheslav.kushchenko.staff.vpn.data.chat.enum.ChatType
import de.vyacheslav.kushchenko.staff.vpn.data.chat.model.Chat
import de.vyacheslav.kushchenko.staff.vpn.data.chat.repository.ChatRepository
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.UserRole
import de.vyacheslav.kushchenko.staff.vpn.web.exception.base.NotFoundException
import eu.vendeli.tgbot.api.chat.getChatMember
import eu.vendeli.tgbot.types.chat.ChatMember
import eu.vendeli.tgbot.types.internal.getOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val userService: UserService,
    private val telegramNotificationService: TelegramNotificationService,
) {
    fun listChats(): List<Chat> =
        chatRepository.findAll().map { it.asModel() }

    fun listPendingChats(): List<Chat> =
        chatRepository.findAllByStatus(ChatStatus.PENDING).map { it.asModel() }

    fun getById(id: UUID): Chat =
        chatRepository.findById(id).orElseThrow { NotFoundException("Chat not found") }.asModel()

    fun listApprovedChats(): List<Chat> =
        chatRepository.findAllByStatus(ChatStatus.APPROVED).map { it.asModel() }

    fun registerChat(
        chatId: Long,
        type: ChatType,
        title: String?,
        username: String?,
        initiatedBy: UUID,
        initiatedByRole: UserRole,
        joinedAt: OffsetDateTime
    ): Chat {
        val existing = chatRepository.findByChatId(chatId)
        val approvedBy = if (initiatedByRole == UserRole.OWNER) initiatedBy else null
        val approvedAt = if (initiatedByRole == UserRole.OWNER) joinedAt else null
        val status = if (initiatedByRole == UserRole.OWNER) ChatStatus.APPROVED else ChatStatus.PENDING
        val entity = if (existing == null) {
            Chat(
                chatId = chatId,
                type = type,
                title = title,
                username = username,
                status = status,
                joinedAt = joinedAt,
                initiatedBy = initiatedBy,
                initiatedByRole = initiatedByRole,
                approvedBy = approvedBy,
                approvedAt = approvedAt
            ).asEntity()
        } else {
            existing.copy(
                type = type,
                title = title,
                username = username,
                status = status,
                joinedAt = joinedAt,
                initiatedBy = initiatedBy,
                initiatedByRole = initiatedByRole,
                approvedBy = approvedBy,
                approvedAt = approvedAt
            )
        }

        return chatRepository.save(entity).asModel()
    }

    fun markRejected(chatId: Long): Chat? {
        val existing = chatRepository.findByChatId(chatId) ?: return null
        val updated = existing.copy(status = ChatStatus.REJECTED)
        return chatRepository.save(updated).asModel()
    }

    fun approveChat(id: UUID, approvedBy: UUID): Chat {
        val existing = getById(id)

        val updated = existing.copy(
            status = ChatStatus.APPROVED,
            approvedBy = approvedBy,
            approvedAt = OffsetDateTime.now(ZoneOffset.UTC)
        )

        val saved = chatRepository.save(updated.asEntity()).asModel()
        existing.initiatedBy
            ?.let(userService::getByIdOrNull)
            ?.let { telegramNotificationService.notifyChatRequestApproved(it, saved) }
        return saved
    }

    fun rejectChat(id: UUID): Chat {
        val existing = getById(id)
        val updated = existing.copy(
            status = ChatStatus.REJECTED,
            approvedBy = null,
            approvedAt = null
        )

        val saved = chatRepository.save(updated.asEntity()).asModel()
        existing.initiatedBy
            ?.let(userService::getByIdOrNull)
            ?.let { telegramNotificationService.notifyChatRequestRejected(it, saved) }
        return saved
    }

    fun hasUserInApprovedChat(userId: Long, bot: eu.vendeli.tgbot.TelegramBot): Boolean {
        val chats = listApprovedChats()
        if (chats.isEmpty()) return false

        return chats.any { chat ->
            val member = runBlocking {
                getChatMember(userId).sendReturning(chat.chatId, bot).getOrNull()
            }
            isActiveMember(member)
        }
    }

    private fun isActiveMember(member: ChatMember?): Boolean =
        when (member) {
            is ChatMember.Member -> true
            is ChatMember.Administrator -> true
            is ChatMember.Owner -> true
            else -> false
        }
}
