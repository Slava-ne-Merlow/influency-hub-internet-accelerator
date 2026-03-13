package de.vyacheslav.kushchenko.staff.vpn.web.exception.base

import de.vyacheslav.kushchenko.staff.vpn.web.response.WebErrorException

class ConflictException(message: String = "Conflict") : WebErrorException(message, 409)