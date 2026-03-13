package de.vyacheslav.kushchenko.staff.vpn.web.exception.base

import de.vyacheslav.kushchenko.staff.vpn.web.response.WebErrorException

class PayloadTooLargeException(message: String = "Payload is too large") : WebErrorException(message, 413)