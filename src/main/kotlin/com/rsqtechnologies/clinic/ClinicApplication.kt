package com.rsqtechnologies.clinic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ClinicApplication

fun main(args: Array<String>) {
    runApplication<ClinicApplication>(*args)
}
