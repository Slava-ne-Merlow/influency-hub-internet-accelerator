package de.vyacheslav.kushchenko.staff.vpn.data.auth.exception

import de.vyacheslav.kushchenko.staff.vpn.web.response.WebErrorException

class BadPasswordException(message: String = "Bad password") : WebErrorException(message, 400)