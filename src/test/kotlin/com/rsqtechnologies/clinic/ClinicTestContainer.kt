package com.rsqtechnologies.clinic

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.ParameterizedTypeReference
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.ext.ScriptUtils

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

class ClinicTestContainer(imageName: String) : PostgreSQLContainer<ClinicTestContainer>(imageName) {

    companion object Instance {
        val instance: ClinicTestContainer = ClinicTestContainer("postgres:alpine")
            .withExposedPorts(5432)
            .withUsername("clinic")
            .withPassword("clinic")
            .withDatabaseName("clinic")

        init {
            instance.start()
        }

        fun createDropDatabase() {
            ScriptUtils.runInitScript(instance.databaseDelegate, "init.sql")
        }
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.jpa.show-sql=true",
                "spring.datasource.url=" + instance.jdbcUrl,
                "spring.datasource.username=" + instance.username,
                "spring.datasource.password=" + instance.password
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}

