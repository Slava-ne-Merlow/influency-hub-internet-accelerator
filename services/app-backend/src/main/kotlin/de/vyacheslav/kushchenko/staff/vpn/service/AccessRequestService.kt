package de.vyacheslav.kushchenko.staff.vpn.service

import de.vyacheslav.kushchenko.staff.vpn.data.request.dao.AccessRequestEntity.Companion.asEntity
import de.vyacheslav.kushchenko.staff.vpn.data.request.dao.AccessRequestEntity.Companion.asModel
import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestDecisionType
import de.vyacheslav.kushchenko.staff.vpn.data.request.enum.AccessRequestStatus
import de.vyacheslav.kushchenko.staff.vpn.data.request.model.AccessRequest
import de.vyacheslav.kushchenko.staff.vpn.data.request.repository.AccessRequestRepository
import de.vyacheslav.kushchenko.staff.vpn.data.user.enum.ManualAccessType
import de.vyacheslav.kushchenko.staff.vpn.web.exception.base.NotFoundException
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class AccessRequestService(
    private val accessRequestRepository: AccessRequestRepository,
    private val userService: UserService,
    private val telegramNotificationService: TelegramNotificationService,
) {
    fun listAll(): List<AccessRequest> =
        accessRequestRepository.findAll().map { it.asModel() }

    fun listPending(): List<AccessRequest> =
        accessRequestRepository.findAllByStatusOrderByCreatedAtDesc(AccessRequestStatus.PENDING).map { it.asModel() }

    fun getById(id: UUID): AccessRequest =
        accessRequestRepository.findById(id).orElseThrow { NotFoundException("Access request not found") }.asModel()

    fun getLatestForUser(userId: UUID): AccessRequest? =
        accessRequestRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)?.asModel()

    fun createOrGetPending(userId: UUID): AccessRequest {
        val existing = accessRequestRepository
            .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, AccessRequestStatus.PENDING)
        if (existing != null) return existing.asModel()

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val request = AccessRequest(
            userId = userId,
            status = AccessRequestStatus.PENDING,
            createdAt = now,
            decisionType = null
        )
        return accessRequestRepository.save(request.asEntity()).asModel()
    }

    fun approve(id: UUID, resolvedBy: UUID, decisionType: AccessRequestDecisionType): AccessRequest {
        val existing = getById(id)
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val accessType = when (decisionType) {
            AccessRequestDecisionType.APPROVE_3_MONTH -> ManualAccessType.THREE_MONTHS
            AccessRequestDecisionType.APPROVE_FOREVER -> ManualAccessType.FOREVER
            AccessRequestDecisionType.REJECT -> throw IllegalArgumentException("Reject decision cannot approve request")
        }
        val accessUntil = when (accessType) {
            ManualAccessType.THREE_MONTHS -> now.plusMonths(3)
            ManualAccessType.FOREVER,
            ManualAccessType.NONE -> null
        }
        val updatedUser = userService.grantManualAccess(existing.userId, accessType, accessUntil)
        val updated = existing.copy(
            status = AccessRequestStatus.APPROVED,
            decisionType = decisionType,
            resolvedAt = now,
            resolvedBy = resolvedBy
        )
        val saved = accessRequestRepository.save(updated.asEntity()).asModel()
        telegramNotificationService.notifyAccessRequestApproved(updatedUser, decisionType, accessUntil)
        return saved
    }

    fun reject(id: UUID, resolvedBy: UUID): AccessRequest {
        val existing = getById(id)
        val updated = existing.copy(
            status = AccessRequestStatus.REJECTED,
            decisionType = AccessRequestDecisionType.REJECT,
            resolvedAt = OffsetDateTime.now(ZoneOffset.UTC),
            resolvedBy = resolvedBy
        )

        val saved = accessRequestRepository.save(updated.asEntity()).asModel()
        telegramNotificationService.notifyAccessRequestRejected(userService.getById(existing.userId))
        return saved
    }
}
