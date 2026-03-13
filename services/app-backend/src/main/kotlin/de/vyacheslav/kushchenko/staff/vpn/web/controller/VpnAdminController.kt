package de.vyacheslav.kushchenko.staff.vpn.web.controller

import de.vyacheslav.kushchenko.staff.vpn.api.VpnAdminApi
import de.vyacheslav.kushchenko.staff.vpn.api.model.VpnOnlineUserDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.VpnSummaryDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.VpnUserStatsDto
import de.vyacheslav.kushchenko.staff.vpn.api.model.VpnUserTrafficDto
import de.vyacheslav.kushchenko.staff.vpn.service.VpnAdminDashboardService
import de.vyacheslav.kushchenko.staff.vpn.util.ok
import de.vyacheslav.kushchenko.staff.vpn.web.security.annotation.IsAdmin
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class VpnAdminController(
    private val vpnAdminDashboardService: VpnAdminDashboardService,
) : VpnAdminApi {

    @IsAdmin
    override fun getVpnSummary(): ResponseEntity<VpnSummaryDto> =
        vpnAdminDashboardService.getSummary().let {
            VpnSummaryDto(
                totalUsers = it.totalUsers,
                activeUsers = it.activeUsers,
            ).ok()
        }

    @IsAdmin
    override fun getVpnUsers(): ResponseEntity<List<VpnUserStatsDto>> =
        vpnAdminDashboardService.getUsers().map {
            VpnUserStatsDto(
                email = it.email,
                online = it.online,
                uplink = it.uplink,
                downlink = it.downlink,
            )
        }.ok()

    @IsAdmin
    override fun getVpnOnlineUsers(): ResponseEntity<List<VpnOnlineUserDto>> =
        vpnAdminDashboardService.getOnlineUsers().map {
            VpnOnlineUserDto(
                email = it.email,
                online = it.online,
            )
        }.ok()

    @IsAdmin
    override fun getVpnUserStats(email: String): ResponseEntity<VpnUserTrafficDto> =
        vpnAdminDashboardService.getUserStats(email).let {
            VpnUserTrafficDto(
                email = it.email,
                uplink = it.uplink,
                downlink = it.downlink,
            ).ok()
        }

    @IsAdmin
    override fun exportVpnStats(): ResponseEntity<String> =
        ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vpn-stats.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(vpnAdminDashboardService.exportStatsCsv())
}
