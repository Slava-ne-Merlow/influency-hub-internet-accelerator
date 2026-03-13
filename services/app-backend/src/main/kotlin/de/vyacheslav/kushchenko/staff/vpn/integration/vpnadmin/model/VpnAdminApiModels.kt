package de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateVpnUserRequest(
    val email: String,
    val uuid: String,
    val level: Int = 0,
    val flow: String? = null,
)

data class VpnAdminUsersResponse(
    val items: List<VpnAdminUserIdentifier>,
)

data class VpnAdminUserIdentifier(
    val email: String,
)

data class VpnAdminUsersStatsResponse(
    val items: List<VpnAdminUserTrafficStats>,
)

data class VpnAdminUserTrafficStats(
    val email: String,
    val uplink: Long,
    val downlink: Long,
)

data class VpnAdminOnlineUsersResponse(
    val items: List<VpnAdminOnlineUser>,
)

data class VpnAdminOnlineUser(
    val email: String,
    val online: Boolean,
)

data class CreateVpnUserResponse(
    val status: String,
    val user: VpnAdminUser,
    val connection: VpnAdminConnection,
)

data class VpnAdminUser(
    val email: String,
    val uuid: String,
)

data class VpnAdminConnection(
    val uri: String,
    val host: String,
    val port: Int,
    val sni: String,
    @param:JsonProperty("public_key")
    @field:JsonProperty("public_key")
    val publicKey: String,
    @param:JsonProperty("short_id")
    @field:JsonProperty("short_id")
    val shortId: String,
)

data class VpnAdminErrorResponse(
    val detail: String,
)
