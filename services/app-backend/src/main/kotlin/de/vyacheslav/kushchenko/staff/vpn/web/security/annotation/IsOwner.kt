package de.vyacheslav.kushchenko.staff.vpn.web.security.annotation

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize("hasRole('OWNER')")
annotation class IsOwner
