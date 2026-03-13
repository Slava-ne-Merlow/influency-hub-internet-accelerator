package de.vyacheslav.kushchenko.staff.vpn.web.controller

import de.vyacheslav.kushchenko.staff.vpn.api.RequestsApi
import de.vyacheslav.kushchenko.staff.vpn.api.model.AccessRequestDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.ApproveAccessRequestRequest
import de.vyacheslav.kushchenko.staff.vpn.data.request.model.toDto
import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestDecisionType
import de.vyacheslav.kushchenko.staff.vpn.service.AccessRequestService
import de.vyacheslav.kushchenko.staff.vpn.util.getRequestUser
import de.vyacheslav.kushchenko.staff.vpn.util.ok
import de.vyacheslav.kushchenko.staff.vpn.web.security.annotation.IsAdmin
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccessRequestController(
    private val accessRequestService: AccessRequestService,
) : RequestsApi {

    @IsAdmin
    override fun getRequests(): ResponseEntity<List<AccessRequestDto>> =
        accessRequestService.listAll().map { it.toDto() }.ok()

    @IsAdmin
    override fun getPendingRequests(): ResponseEntity<List<AccessRequestDto>> =
        accessRequestService.listPending().map { it.toDto() }.ok()

    @IsAdmin
    override fun getRequestById(id: UUID): ResponseEntity<AccessRequestDto> =
        accessRequestService.getById(id).toDto().ok()

    @IsAdmin
    override fun approveRequest(id: UUID, approveAccessRequestRequest: ApproveAccessRequestRequest): ResponseEntity<AccessRequestDto> =
        accessRequestService.approve(
            id = id,
            resolvedBy = getRequestUser().id!!,
            decisionType = AccessRequestDecisionType.valueOf(approveAccessRequestRequest.decisionType.name)
        ).toDto().ok()

    @IsAdmin
    override fun rejectRequest(id: UUID): ResponseEntity<AccessRequestDto> =
        accessRequestService.reject(id, getRequestUser().id!!).toDto().ok()
}
