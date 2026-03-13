package de.vyacheslav.kushchenko.staff.vpn.data.chat.dao

import de.vyacheslav.kushchenko.staff.vpn.data.chat.enum.ChatStatus
import de.vyacheslav.kushchenko.staff.vpn.data.chat.enum.ChatType
import de.vyacheslav.kushchenko.staff.vpn.data.chat.model.Chat
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.UserRole
import de.vyacheslav.kushchenko.staff.vpn.util.model.EntityConverter
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "trusted_chats")
data class ChatEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "chat_id", unique = true)
    val chatId: Long,

    @Enumerated(EnumType.STRING)
    val type: ChatType,

    val title: String? = null,

    val username: String? = null,

    @Enumerated(EnumType.STRING)
    val status: ChatStatus,

    @Column(name = "joined_at")
    val joinedAt: OffsetDateTime,

    @Column(name = "initiated_by")
    val initiatedBy: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "initiated_by_role")
    val initiatedByRole: UserRole? = null,

    @Column(name = "approved_by")
    val approvedBy: UUID? = null,

    @Column(name = "approved_at")
    val approvedAt: OffsetDateTime? = null,
) {
    companion object : EntityConverter<Chat, ChatEntity> {
        override fun ChatEntity.asModel() = Chat(
            id = id,
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
        )

        override fun Chat.asEntity() = ChatEntity(
            id = id,
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
        )
    }
}
