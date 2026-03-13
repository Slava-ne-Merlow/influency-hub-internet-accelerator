package de.vyacheslav.kushchenko.staff.vpn.config

import de.vyacheslav.kushchenko.staff.vpn.service.UserService
import de.vyacheslav.kushchenko.staff.vpn.web.exception.base.NotFoundException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
class SecurityConfig {

    @Bean
    fun authenticationManager(
        httpSecurity: HttpSecurity, authenticationProvider: AuthenticationProvider
    ): AuthenticationManager = httpSecurity.getSharedObject(AuthenticationManagerBuilder::class.java)
        .authenticationProvider(authenticationProvider)
        .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(userService: UserService): UserDetailsService = UserDetailsService {
        try {
            val telegramId = it.toLongOrNull() ?: throw BadCredentialsException("Invalid telegram id")
            userService.getByTelegramId(telegramId)
        } catch (e: NotFoundException) {
            throw BadCredentialsException("User details not found by telegram id")
        }
    }
}
