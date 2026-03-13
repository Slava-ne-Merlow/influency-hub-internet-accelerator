package de.vyacheslav.kushchenko.staff.vpn.web.exception.base

import de.vyacheslav.kushchenko.staff.vpn.web.response.WebErrorException

class NotFoundException(message: String = "Not found") : WebErrorException(message, 404)