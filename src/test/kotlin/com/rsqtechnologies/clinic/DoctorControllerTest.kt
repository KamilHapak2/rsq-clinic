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
        val response = executeGetDoctors()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    internal fun shouldCreateDoctor() {

        // given
        val givenName = "Joe"
        executePostDoctors(CreateDoctorCommand(givenName))

        // when
        val response = executeGetDoctors()

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.size).isEqualTo(1)
        assertThat(response.body!![0].name).isEqualTo(givenName)
    }


    @Test
    internal fun shouldDeleteDoctor() {

        // given
        val givenName = "Joe"
        val doctor = executePostDoctors(CreateDoctorCommand(givenName)).body

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
        val givenName = "Joe"
        val doctor = executePostDoctors(CreateDoctorCommand(givenName)).body
        val expectedName = "Doe"

        // when
        val updateResponse = executePutDoctors(CreateDoctorCommand(expectedName), doctor!!.id)

        // then
        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(updateResponse.body).isNotNull
        assertThat(updateResponse.body!!.name).isEqualTo(expectedName)

        val getResponse = executeGetDoctors()
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(1)
        assertThat(getResponse.body!![0].name).isEqualTo(expectedName)

    }

    private fun executePostDoctors(createDoctorCommand: CreateDoctorCommand): ResponseEntity<DoctorView> {
        return restTemplate.exchange(
            "/doctors",
            HttpMethod.POST,
            HttpEntity(createDoctorCommand),
            DoctorView::class.java
        )
    }

    private fun executePutDoctors(createDoctorCommand: CreateDoctorCommand, id: Long): ResponseEntity<DoctorView> {
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
}
