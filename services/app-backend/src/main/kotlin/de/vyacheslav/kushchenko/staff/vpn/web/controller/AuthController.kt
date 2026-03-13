package de.vyacheslav.kushchenko.staff.vpn.web.controller

import de.vyacheslav.kushchenko.staff.vpn.api.AuthApi
import de.vyacheslav.kushchenko.staff.vpn.api.model.AuthResponse
import de.vyacheslav.kushchenko.staff.vpn.api.model.TelegramAuthRequest
import de.vyacheslav.kushchenko.staff.vpn.service.AuthenticationService
import de.vyacheslav.kushchenko.staff.vpn.util.ok
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class AuthController(
    private val authenticationService: AuthenticationService
) : AuthApi {

    override fun telegramAuth(telegramAuthRequest: TelegramAuthRequest): ResponseEntity<AuthResponse> {
        val logger = LoggerFactory.getLogger(this.javaClass)
        logger.warn(telegramAuthRequest.toString())
        val response = authenticationService.telegramAuth(telegramAuthRequest.initData)
        logger.error(response.toString())
        return response.ok()
    }

}
