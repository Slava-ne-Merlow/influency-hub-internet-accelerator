package de.vyacheslav.kushchenko.staff.vpn

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class StaffAccessVPNApplication

fun main(args: Array<String>) {
    runApplication<StaffAccessVPNApplication>(*args)
}

fun run(
    args: Array<String>,
    init: SpringApplication.() -> Unit = {},
): ConfigurableApplicationContext {
    return runApplication<StaffAccessVPNApplication>(*args, init = init)
}
