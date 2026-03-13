package de.vyacheslav.kushchenko.staff.vpn.web.response

open class WebErrorException(
    val error: StatusResponse.Error
) : RuntimeException() {
    constructor(message: String, httpStatus: Int) : this(StatusResponse.Error(message, httpStatus))
}
