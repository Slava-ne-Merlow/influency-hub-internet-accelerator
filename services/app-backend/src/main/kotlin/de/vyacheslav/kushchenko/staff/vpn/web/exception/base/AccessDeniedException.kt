package de.vyacheslav.kushchenko.staff.vpn.web.exception.base

import de.vyacheslav.kushchenko.staff.vpn.web.response.WebErrorException

class AccessDeniedException(message: String = "Forbidden") : WebErrorException(message, 403)