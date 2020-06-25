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
class DoctorControllerTest {

    @BeforeEach
    internal fun setUp() {
        ClinicTestContainer.createDropDatabase()
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    internal fun shouldGetDoctors() {

        // given
        executePostDoctors(CreateOrUpdateDoctorCommand(name = "Joe", surname = "Doe", spec = "Dent"))
        executePostDoctors(CreateOrUpdateDoctorCommand(name = "Daniel", surname = "Jackson", spec = "Surgeon"))
        executePostDoctors(CreateOrUpdateDoctorCommand(name = "Jim", surname = "Bim", spec = "Psychologist"))

        // when
        val response = executeGetDoctors()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.size).isEqualTo(3)
    }

    @Test
    internal fun shouldCreateDoctor() {

        // given
        val givenCreateCommand =
            CreateOrUpdateDoctorCommand(name = "Joe", surname = "Doe", spec = "dent")
        executePostDoctors(givenCreateCommand)

        // when
        val response = executeGetDoctors()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.size).isEqualTo(1)
        assertEquals(response.body!![0], givenCreateCommand)
    }

    @Test
    internal fun shouldDeleteDoctor() {

        // given
        val doctor =
            executePostDoctors(CreateOrUpdateDoctorCommand(name = "Joe", surname = "Doe", spec = "dent")).body

        // when
        val deleteResponse = executeDeleteDoctors(doctor!!.id)

        // then
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val getResponse = executeGetDoctors()
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(0)
    }

    @Test
    internal fun shouldUpdateDoctor() {

        // given
        val doctor = executePostDoctors(
            CreateOrUpdateDoctorCommand(name = "Joe", surname = "Doe", spec = "dent")
        ).body
        val givenUpdateCommand = CreateOrUpdateDoctorCommand("Jim", "Bim", "surgeon")

        // when
        val updateResponse =
            executePutDoctors(givenUpdateCommand, doctor!!.id)

        // then
        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertEquals(updateResponse.body, givenUpdateCommand)

        val getResponse = executeGetDoctors()
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(1)
        assertEquals(getResponse.body!![0], givenUpdateCommand)
    }

    private fun assertEquals(
        actualDoctor: DoctorView?,
        expectedDoctor: CreateOrUpdateDoctorCommand
    ) {
        assertThat(actualDoctor).isNotNull
        assertThat(actualDoctor!!.name).isEqualTo(expectedDoctor.name)
        assertThat(actualDoctor.surname).isEqualTo(expectedDoctor.surname)
        assertThat(actualDoctor.spec).isEqualTo(expectedDoctor.spec)
    }

    private fun executePostDoctors(createDoctorCommand: CreateOrUpdateDoctorCommand) =
        DoctorTestUtils.executePostDoctors(restTemplate, createDoctorCommand)

    private fun executePutDoctors(
        createDoctorCommand: CreateOrUpdateDoctorCommand,
        id: Long
    ): ResponseEntity<DoctorView> {
        return restTemplate.exchange(
            "/doctors/$id",
            HttpMethod.PUT,
            HttpEntity(createDoctorCommand),
            DoctorView::class.java
        )
    }

    private fun executeDeleteDoctors(id: Long): ResponseEntity<Void> {
        return restTemplate.exchange(
            "/doctors/$id",
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            Void::class.java
        )
    }

    private fun executeGetDoctors(): ResponseEntity<List<DoctorView>> {
        return restTemplate.exchange(
            "/doctors",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            typeRef<List<DoctorView>>()
        )
    }

    object DoctorTestUtils {

        fun executePostDoctors(
            restTemplate: TestRestTemplate,
            createDoctorCommand: CreateOrUpdateDoctorCommand
        ): ResponseEntity<DoctorView> = restTemplate.exchange(
            "/doctors",
            HttpMethod.POST,
            HttpEntity(createDoctorCommand),
            DoctorView::class.java
        )
    }
}
