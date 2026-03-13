package de.vyacheslav.kushchenko.staff.vpn.web.controller

import de.vyacheslav.kushchenko.staff.vpn.api.ChatRequestsApi
import de.vyacheslav.kushchenko.staff.vpn.api.model.ChatDto
import de.vyacheslav.kushchenko.staff.vpn.data.chat.model.toDto
import de.vyacheslav.kushchenko.staff.vpn.service.ChatService
import de.vyacheslav.kushchenko.staff.vpn.util.getRequestUser
import de.vyacheslav.kushchenko.staff.vpn.util.ok
import de.vyacheslav.kushchenko.staff.vpn.web.security.annotation.IsAdmin
import de.vyacheslav.kushchenko.staff.vpn.web.security.annotation.IsOwner
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ChatRequestController(
    private val chatService: ChatService,
) : ChatRequestsApi {

    @IsAdmin
    override fun getChatRequests(): ResponseEntity<List<ChatDto>> =
        chatService.listPendingChats().map { it.toDto() }.ok()

    @IsAdmin
    override fun getChatRequestById(id: UUID): ResponseEntity<ChatDto> =
        chatService.getById(id).toDto().ok()

    @IsOwner
    override fun approveChatRequest(id: UUID, body: Any?): ResponseEntity<ChatDto> =
        chatService.approveChat(id, getRequestUser().id!!).toDto().ok()

    @IsOwner
    override fun rejectChatRequest(id: UUID): ResponseEntity<ChatDto> =
        chatService.rejectChat(id).toDto().ok()
}
