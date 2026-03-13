package de.vyacheslav.kushchenko.staff.vpn.service

import de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.VpnAdminGateway
import org.springframework.stereotype.Service

@Service
class VpnAdminDashboardService(
    private val vpnAdminGateway: VpnAdminGateway,
) {

    fun getSummary(): VpnDashboardSummary {
        val users = vpnAdminGateway.getUsers().items
        val onlineUsers = vpnAdminGateway.getOnlineUsers().items.count { it.online }

        return VpnDashboardSummary(
            totalUsers = users.size.toLong(),
            activeUsers = onlineUsers.toLong(),
        )
    }

    fun getUsers(): List<VpnDashboardUser> {
        val users = vpnAdminGateway.getUsers().items
        val onlineUsers = vpnAdminGateway.getOnlineUsers().items
            .filter { it.online }
            .associateBy { it.email }
        val statsByEmail = vpnAdminGateway.getUsersStats().items.associateBy { it.email }

        return users.map { user ->
            val stats = statsByEmail[user.email]
            VpnDashboardUser(
                email = user.email,
                online = onlineUsers.containsKey(user.email),
                uplink = stats?.uplink ?: 0,
                downlink = stats?.downlink ?: 0,
            )
        }.sortedBy { it.email }
    }

    fun getOnlineUsers(): List<VpnDashboardOnlineUser> =
        vpnAdminGateway.getOnlineUsers().items
            .filter { it.online }
            .map { VpnDashboardOnlineUser(email = it.email, online = true) }
            .sortedBy { it.email }

    fun getUserStats(email: String): VpnDashboardUserTraffic =
        vpnAdminGateway.getUserStats(email).let {
            VpnDashboardUserTraffic(
                email = it.email,
                uplink = it.uplink,
                downlink = it.downlink,
            )
        }

    fun exportStatsCsv(): String {
        val rows = getUsers()
        val header = "email,online,uplink,downlink"
        val content = rows.joinToString("\n") { row ->
            listOf(
                escapeCsv(row.email),
                row.online.toString(),
                row.uplink.toString(),
                row.downlink.toString(),
            ).joinToString(",")
        }

        return if (content.isEmpty()) "$header\n" else "$header\n$content\n"
    }

    private fun escapeCsv(value: String): String {
        if (value.none { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            return value
        }

        return "\"" + value.replace("\"", "\"\"") + "\""
    }
}

data class VpnDashboardSummary(
    val totalUsers: Long,
    val activeUsers: Long,
)

data class VpnDashboardUser(
    val email: String,
    val online: Boolean,
    val uplink: Long,
    val downlink: Long,
)

data class VpnDashboardOnlineUser(
    val email: String,
    val online: Boolean,
)

data class VpnDashboardUserTraffic(
    val email: String,
    val uplink: Long,
    val downlink: Long,
)
