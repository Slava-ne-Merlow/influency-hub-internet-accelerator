package de.vyacheslav.kushchenko.staff.vpn.web.exception.base

import de.vyacheslav.kushchenko.staff.vpn.web.response.WebErrorException

class UnsupportedMediaTypeException(message: String = "Unsupported media type") : WebErrorException(message, 409)