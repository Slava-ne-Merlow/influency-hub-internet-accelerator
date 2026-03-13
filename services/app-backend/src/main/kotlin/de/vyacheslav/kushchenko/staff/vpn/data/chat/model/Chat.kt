package de.vyacheslav.kushchenko.staff.vpn.data.chat.model

import de.vyacheslav.kushchenko.staff.vpn.api.model.ChatDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.ChatStatus as ChatStatusDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.ChatType as ChatTypeDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.Role as RoleDto
import de.vyacheslav.kushchenko.staff.vpn.data.chat.enum.ChatStatus
import de.vyacheslav.kushchenko.staff.vpn.data.chat.enum.ChatType
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.UserRole
import java.time.OffsetDateTime
import java.util.UUID

data class Chat(
    val id: UUID? = null,
    val chatId: Long,
    val type: ChatType,
    val title: String? = null,
    val username: String? = null,
    val status: ChatStatus,
    val joinedAt: OffsetDateTime,
    val initiatedBy: UUID? = null,
    val initiatedByRole: UserRole? = null,
    val approvedBy: UUID? = null,
    val approvedAt: OffsetDateTime? = null,
)

fun Chat.toDto() = ChatDto(
    id = this.id!!,
    chatId = chatId,
    type = ChatTypeDto.valueOf(this.type.name),
    title = title,
    username = username,
    status = ChatStatusDto.valueOf(this.status.name),
    joinedAt = joinedAt,
    initiatedBy = initiatedBy,
    initiatedByRole = initiatedByRole?.let { RoleDto.valueOf(it.name) },
    approvedBy = approvedBy,
    approvedAt = approvedAt
)
