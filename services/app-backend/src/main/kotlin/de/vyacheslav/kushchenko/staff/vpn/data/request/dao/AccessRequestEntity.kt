package de.vyacheslav.kushchenko.staff.vpn.data.request.dao

import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestDecisionType
import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestStatus
import de.vyacheslav.kushchenko.staff.vpn.data.request.model.AccessRequest
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
@Table(name = "access_requests")
data class AccessRequestEntity(
    @Id
    @GeneratedValue
    val id: UUID? = null,

    @Column(name = "user_id")
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    val status: AccessRequestStatus,

    @Column(name = "created_at")
    val createdAt: OffsetDateTime,

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = true)
    val decisionType: AccessRequestDecisionType? = null,

    @Column(name = "resolved_at")
    val resolvedAt: OffsetDateTime? = null,

    @Column(name = "resolved_by")
    val resolvedBy: UUID? = null,
) {
    companion object : EntityConverter<AccessRequest, AccessRequestEntity> {
        override fun AccessRequestEntity.asModel() = AccessRequest(
            id = id,
            userId = userId,
            status = status,
            createdAt = createdAt,
            decisionType = decisionType,
            resolvedAt = resolvedAt,
            resolvedBy = resolvedBy
        )

        override fun AccessRequest.asEntity() = AccessRequestEntity(
            id = id,
            userId = userId,
            status = status,
            createdAt = createdAt,
            decisionType = decisionType,
            resolvedAt = resolvedAt,
            resolvedBy = resolvedBy
        )
    }
}
