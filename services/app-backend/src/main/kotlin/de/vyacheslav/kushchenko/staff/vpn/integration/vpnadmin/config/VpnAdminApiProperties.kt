package de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import java.time.Duration

@Component
@Validated
@ConfigurationProperties(prefix = "app.vpn.admin")
class VpnAdminApiProperties {
    @field:NotBlank
    lateinit var baseUrl: String

    @field:NotBlank
    lateinit var serviceKey: String

    @field:NotNull
    var connectTimeout: Duration = Duration.ofSeconds(5)

    @field:NotNull
    var readTimeout: Duration = Duration.ofSeconds(10)
}
