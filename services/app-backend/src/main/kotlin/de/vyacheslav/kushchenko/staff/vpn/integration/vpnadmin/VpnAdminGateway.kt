package de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin

import de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.model.CreateVpnUserRequest
import de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.model.CreateVpnUserResponse
import de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.model.VpnAdminOnlineUsersResponse
import de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.model.VpnAdminUserTrafficStats
import de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.model.VpnAdminUsersResponse
import de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.model.VpnAdminUsersStatsResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

@Component
class VpnAdminGateway(
    private val vpnAdminApiRestClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(VpnAdminGateway::class.java)

    fun createUser(request: CreateVpnUserRequest): CreateVpnUserResponse =
        execute("create VPN user") {
            vpnAdminApiRestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(CreateVpnUserResponse::class.java)
                ?: throw IllegalStateException("VPN admin API returned empty response body")
        }

    fun getUsers(): VpnAdminUsersResponse =
        execute("get VPN users") {
            vpnAdminApiRestClient
                .get()
                .uri("/users")
                .retrieve()
                .body(VpnAdminUsersResponse::class.java)
                ?: throw IllegalStateException("VPN admin API returned empty response body")
        }

    fun getUserStats(email: String): VpnAdminUserTrafficStats =
        execute("get VPN user stats") {
            vpnAdminApiRestClient
                .get()
                .uri("/stats/users/{email}", email)
                .retrieve()
                .body(VpnAdminUserTrafficStats::class.java)
                ?: throw IllegalStateException("VPN admin API returned empty response body")
        }

    fun getUsersStats(): VpnAdminUsersStatsResponse =
        execute("get VPN users stats") {
            vpnAdminApiRestClient
                .get()
                .uri("/stats/users")
                .retrieve()
                .body(VpnAdminUsersStatsResponse::class.java)
                ?: throw IllegalStateException("VPN admin API returned empty response body")
        }

    fun getOnlineUsers(): VpnAdminOnlineUsersResponse =
        execute("get VPN online users") {
            vpnAdminApiRestClient
                .get()
                .uri("/online")
                .retrieve()
                .body(VpnAdminOnlineUsersResponse::class.java)
                ?: throw IllegalStateException("VPN admin API returned empty response body")
        }

    private fun <T> execute(action: String, block: () -> T): T =
        try {
            block()
        } catch (e: RestClientResponseException) {
            log.error(
                "VPN admin API {} failed status={} body={}",
                action,
                e.statusCode.value(),
                e.responseBodyAsString,
            )
            throw IllegalStateException("VPN admin API $action failed: ${e.statusCode.value()}", e)
        } catch (e: RestClientException) {
            log.error("VPN admin API {} failed", action, e)
            throw IllegalStateException("VPN admin API $action failed", e)
        }
}
