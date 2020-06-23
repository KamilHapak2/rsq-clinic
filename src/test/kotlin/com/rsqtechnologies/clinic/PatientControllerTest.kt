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
    internal fun shouldGETPatients() {

        // when
        val response = executeGetPatients()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    internal fun shouldCreatePatient() {

        // given
        val givenName = "Joe"
        val givenSurname = "Doe"
        val givenAddress = "Jim 123, 12-222 New York"
        executePostPatient(
            CreateOrUpdatePatientCommand(
                name = givenName,
                surname = givenSurname,
                address = givenAddress
            )
        )

        // when
        val response = executeGetPatients()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.size).isEqualTo(1)
        assertThat(response.body!![0].name).isEqualTo(givenName)
        assertThat(response.body!![0].surname).isEqualTo(givenSurname)
        assertThat(response.body!![0].address).isEqualTo(givenAddress)
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
        val expectedName = "Jim"
        val expectedSurname = "Bim"
        val expectedAddress = "Jim 123, 12-222 New York"
        // when
        val updateResponse =
            executePutPatient(
                CreateOrUpdatePatientCommand(expectedName, expectedSurname, expectedAddress),
                patient!!.id
            )

        // then
        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(updateResponse.body).isNotNull
        assertThat(updateResponse.body!!.name).isEqualTo(expectedName)
        assertThat(updateResponse.body!!.surname).isEqualTo(expectedSurname)
        assertThat(updateResponse.body!!.address).isEqualTo(expectedAddress)

        val getResponse = executeGetPatients()
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(1)
        assertThat(getResponse.body!![0].name).isEqualTo(expectedName)
        assertThat(getResponse.body!![0].surname).isEqualTo(expectedSurname)
        assertThat(getResponse.body!![0].address).isEqualTo(expectedAddress)

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
