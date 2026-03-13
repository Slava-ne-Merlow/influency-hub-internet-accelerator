package de.vyacheslav.kushchenko.staff.vpn.web.security.annotation

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('OWNER') || hasRole('ADMIN') || hasRole('USER')")
annotation class Authorized
