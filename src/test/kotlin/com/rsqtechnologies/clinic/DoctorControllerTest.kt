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
    internal fun shouldGETDoctors() {

        // when
        val response = executeGetDoctors() // todo dodać jakieś obiekty, + pobieranie po id

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    internal fun shouldCreateDoctor() {

        // given
        val givenName = "Joe"
        val givenSurname = "Doe"
        val givenSpec = "dent"
        executePostDoctors(CreateOrUpdateDoctorCommand(name = givenName, surname = givenSurname, spec = givenSpec))

        // when
        val response = executeGetDoctors()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.size).isEqualTo(1)
        assertThat(response.body!![0].name).isEqualTo(givenName)
        assertThat(response.body!![0].surname).isEqualTo(givenSurname)
        assertThat(response.body!![0].spec).isEqualTo(givenSpec)
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
        val expectedName = "Jim"
        val expectedSurname = "Bim"
        val expectedSpec = "surgeon"

        // when
        val updateResponse =
            executePutDoctors(CreateOrUpdateDoctorCommand(expectedName, expectedSurname, expectedSpec), doctor!!.id)

        // then
        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(updateResponse.body).isNotNull
        assertThat(updateResponse.body!!.name).isEqualTo(expectedName)
        assertThat(updateResponse.body!!.surname).isEqualTo(expectedSurname)
        assertThat(updateResponse.body!!.spec).isEqualTo(expectedSpec)

        val getResponse = executeGetDoctors()
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(1)
        assertThat(getResponse.body!![0].name).isEqualTo(expectedName)
        assertThat(getResponse.body!![0].surname).isEqualTo(expectedSurname)
        assertThat(getResponse.body!![0].spec).isEqualTo(expectedSpec)

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
