package de.vyacheslav.kushchenko.staff.vpn.web.controller

import de.vyacheslav.kushchenko.staff.vpn.api.GroupsApi
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
class GroupController(
    private val chatService: ChatService,
) : GroupsApi {

    @IsAdmin
    override fun getGroups(): ResponseEntity<List<ChatDto>> =
        chatService.listChats().map { it.toDto() }.ok()

    @IsOwner
    override fun approveGroup(id: UUID, body: Any?): ResponseEntity<ChatDto> =
        chatService.approveChat(id, getRequestUser().id!!).toDto().ok()

    @IsOwner
    override fun rejectGroup(id: UUID): ResponseEntity<ChatDto> =
        chatService.rejectChat(id).toDto().ok()
}
