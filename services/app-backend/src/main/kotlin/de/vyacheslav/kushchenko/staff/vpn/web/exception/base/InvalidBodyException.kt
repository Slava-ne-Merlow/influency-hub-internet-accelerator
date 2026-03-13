package de.vyacheslav.kushchenko.staff.vpn.web.exception.base

import de.vyacheslav.kushchenko.staff.vpn.web.response.WebErrorException

class InvalidBodyException(message: String = "Invalid body") : WebErrorException(message, 400)