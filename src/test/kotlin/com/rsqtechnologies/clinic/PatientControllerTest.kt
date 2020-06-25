package com.rsqtechnologies.clinic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [ClinicTestContainer.Initializer::class])
class PatientControllerTest {

    @BeforeEach
    internal fun setUp() {
        ClinicTestContainer.createDropDatabase()
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    internal fun shouldGetPatients() {

        // given
        executePostPatient(
            CreateOrUpdatePatientCommand(
                name = "Joe",
                surname = "Doe",
                address = "Jim 123, 12-222 New York"
            )
        )
        executePostPatient(
            CreateOrUpdatePatientCommand(
                name = "Jim",
                surname = "Bim",
                address = "Bim 123, 12-333 York"
            )
        )

        // when
        val response = executeGetPatients()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.size).isEqualTo(2)
    }

    @Test
    internal fun shouldCreatePatient() {

        // given
        val givenCreateCommand = CreateOrUpdatePatientCommand(
            name = "Joe",
            surname = "Doe",
            address = "Jim 123, 12-222 New York"
        )

        // when
        executePostPatient(
            givenCreateCommand
        )

        // then
        val response = executeGetPatients()
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.size).isEqualTo(1)
        assertEquals(response.body!![0], givenCreateCommand)
    }

    @Test
    internal fun shouldDeletePatient() {

        // given
        val patient =
            executePostPatient(
                CreateOrUpdatePatientCommand(
                    name = "Joe",
                    surname = "Doe",
                    address = "Jim 123, 12-222 New York"
                )
            ).body

        // when
        val deleteResponse = executeDeletePatient(patient!!.id)

        // then
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val getResponse = executeGetPatients()
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(0)
    }

    @Test
    internal fun shouldUpdatePatient() {

        // given
        val patient = executePostPatient(
            CreateOrUpdatePatientCommand(name = "Joe", surname = "Doe", address = "Joe 123, 12-222 York")
        ).body
        val givenUpdateCommand = CreateOrUpdatePatientCommand("Jim", "Bim", "Jim 123, 12-222 New York")

        // when
        val updateResponse =
            executePutPatient(
                givenUpdateCommand,
                patient!!.id
            )

        // then
        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(updateResponse.body).isNotNull
        assertEquals(updateResponse.body, givenUpdateCommand)

        val getResponse = executeGetPatients()
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(1)
        assertEquals(getResponse.body!![0], givenUpdateCommand)
    }

    private fun assertEquals(
        actualPatient: PatientView?,
        expectedPatient: CreateOrUpdatePatientCommand
    ) {
        assertThat(actualPatient).isNotNull
        assertThat(actualPatient!!.name).isEqualTo(expectedPatient.name)
        assertThat(actualPatient.surname).isEqualTo(expectedPatient.surname)
        assertThat(actualPatient.address).isEqualTo(expectedPatient.address)
    }

    private fun executePostPatient(createPatientCommand: CreateOrUpdatePatientCommand) =
        PatientTestUtils.executePostPatient(restTemplate, createPatientCommand)

    private fun executePutPatient(
        createPatientCommand: CreateOrUpdatePatientCommand,
        id: Long
    ): ResponseEntity<PatientView> {
        return restTemplate.exchange(
            "/patients/$id",
            HttpMethod.PUT,
            HttpEntity(createPatientCommand),
            PatientView::class.java
        )
    }

    private fun executeDeletePatient(id: Long): ResponseEntity<Void> {
        return restTemplate.exchange(
            "/patients/$id",
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            Void::class.java
        )
    }

    private fun executeGetPatients(): ResponseEntity<List<PatientView>> {
        return restTemplate.exchange(
            "/patients",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            typeRef<List<PatientView>>()
        )
    }

    object PatientTestUtils {

        fun executePostPatient(
            restTemplate: TestRestTemplate,
            createPatientCommand: CreateOrUpdatePatientCommand
        ): ResponseEntity<PatientView> = restTemplate.exchange(
            "/patients",
            HttpMethod.POST,
            HttpEntity(createPatientCommand),
            PatientView::class.java
        )
    }
}
