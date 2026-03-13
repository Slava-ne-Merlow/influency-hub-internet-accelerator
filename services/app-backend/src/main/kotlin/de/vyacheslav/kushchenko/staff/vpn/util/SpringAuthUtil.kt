package de.vyacheslav.kushchenko.staff.vpn.util

import de.vyacheslav.kushchenko.staff.vpn.data.user.model.User
import org.springframework.security.core.context.SecurityContextHolder

fun getRequestUser(): User {
    val authentication = SecurityContextHolder.getContext().authentication
    return authentication.principal as User
}
