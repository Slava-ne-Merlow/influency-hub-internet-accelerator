package de.vyacheslav.kushchenko.staff.vpn.web.controller

import de.vyacheslav.kushchenko.staff.vpn.api.PingApi
import de.vyacheslav.kushchenko.staff.vpn.api.model.StatusResponse
import de.vyacheslav.kushchenko.staff.vpn.util.ok
import org.springframework.stereotype.Component

@Component
class PingController : PingApi {

    override fun ping() = StatusResponse("АЛЕКСАНДР ШАХОВ Я ВАШ ФАНАТ").ok()

}
