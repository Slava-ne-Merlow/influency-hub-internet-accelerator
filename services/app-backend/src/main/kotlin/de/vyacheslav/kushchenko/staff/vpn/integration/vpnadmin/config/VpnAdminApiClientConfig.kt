package de.vyacheslav.kushchenko.staff.vpn.integration.vpnadmin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class VpnAdminApiClientConfig {

    @Bean
    fun vpnAdminApiRestClient(
        builder: RestClient.Builder,
        properties: VpnAdminApiProperties,
    ): RestClient {
        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(properties.connectTimeout)
            setReadTimeout(properties.readTimeout)
        }

        return builder
            .baseUrl(properties.baseUrl)
            .defaultHeader("X-Service-Key", properties.serviceKey)
            .requestFactory(requestFactory)
            .build()
    }
}
