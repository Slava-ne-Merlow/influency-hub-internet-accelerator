package de.vyacheslav.kushchenko.staff.vpn.web.controller

import de.vyacheslav.kushchenko.staff.vpn.api.ChatsApi
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
class ChatController(
    private val chatService: ChatService,
) : ChatsApi {

    @IsAdmin
    override fun getChats(): ResponseEntity<List<ChatDto>> =
        chatService.listChats().map { it.toDto() }.ok()

    @IsOwner
    override fun approveChat(id: UUID, body: Any?): ResponseEntity<ChatDto> =
        chatService.approveChat(id, getRequestUser().id!!).toDto().ok()

    @IsOwner
    override fun rejectChat(id: UUID): ResponseEntity<ChatDto> =
        chatService.rejectChat(id).toDto().ok()
}
