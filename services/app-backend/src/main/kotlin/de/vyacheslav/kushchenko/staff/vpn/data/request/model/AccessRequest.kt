package de.vyacheslav.kushchenko.staff.vpn.data.request.model

import de.vyacheslav.kushchenko.staff.vpn.api.model.AccessRequestDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.AccessRequestDecisionType as AccessRequestDecisionTypeDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.AccessRequestStatus as AccessRequestStatusDto
import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestDecisionType
import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestStatus
import java.time.OffsetDateTime
import java.util.UUID

data class AccessRequest(
    val id: UUID? = null,
    val userId: UUID,
    val status: AccessRequestStatus,
    val createdAt: OffsetDateTime,
    val decisionType: AccessRequestDecisionType? = null,
    val resolvedAt: OffsetDateTime? = null,
    val resolvedBy: UUID? = null,
)

fun AccessRequest.toDto() = AccessRequestDto(
    id = this.id!!,
    userId = userId,
    status = AccessRequestStatusDto.valueOf(this.status.name),
    createdAt = createdAt,
    decisionType = decisionType?.let { AccessRequestDecisionTypeDto.valueOf(it.name) },
    resolvedAt = resolvedAt,
    resolvedBy = resolvedBy
)
