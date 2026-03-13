package de.vyacheslav.kushchenko.staff.vpn.data.request.repository

import de.vyacheslav.kushchenko.staff.vpn.data.request.dao.AccessRequestEntity
import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AccessRequestRepository : JpaRepository<AccessRequestEntity, UUID> {
    fun findFirstByUserIdAndStatusOrderByCreatedAtDesc(
        userId: UUID,
        status: AccessRequestStatus
    ): AccessRequestEntity?

    fun findFirstByUserIdOrderByCreatedAtDesc(userId: UUID): AccessRequestEntity?

    fun findAllByStatusOrderByCreatedAtDesc(status: AccessRequestStatus): List<AccessRequestEntity>
}
