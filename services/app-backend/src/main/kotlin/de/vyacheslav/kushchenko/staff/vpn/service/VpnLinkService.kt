package de.vyacheslav.kushchenko.staff.vpn.service

import de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.VpnAdminGateway
import de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.model.CreateVpnUserRequest
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.UUID

@Service
class VpnLinkService(
    private val userService: UserService,
    private val vpnAdminGateway: VpnAdminGateway,
) {
    fun getLinkForUser(userId: UUID): String {
        val user = userService.getById(userId)
        val email = buildVpnEmail(user.telegramId)
        val request = CreateVpnUserRequest(
            email = email,
            uuid = buildVpnUserUuid(email).toString(),
        )

        return vpnAdminGateway.createUser(request).connection.uri
    }

    private fun buildVpnEmail(telegramId: Long): String = "tg_$telegramId"

    private fun buildVpnUserUuid(email: String): UUID =
        UUID.nameUUIDFromBytes("vpn-user:$email".toByteArray(StandardCharsets.UTF_8))
}
