package com.rsqtechnologies.clinic

import com.rsqtechnologies.clinic.DoctorControllerTest.DoctorTestUtils.executePostDoctors
import com.rsqtechnologies.clinic.PatientControllerTest.PatientTestUtils.executePostPatient
import com.rsqtechnologies.clinic.VisitControllerTest.VisitTestUtils
import com.rsqtechnologies.clinic.VisitControllerTest.VisitTestUtils.executeGetVisit
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
import java.time.LocalDateTime

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
        executePostDoctors(restTemplate, CreateOrUpdateDoctorCommand(name = "Joe", surname = "Doe", spec = "Dent"))
        executePostDoctors(
            restTemplate,
            CreateOrUpdateDoctorCommand(name = "Daniel", surname = "Jackson", spec = "Surgeon")
        )
        executePostDoctors(
            restTemplate,
            CreateOrUpdateDoctorCommand(name = "Jim", surname = "Bim", spec = "Psychologist")
        )

        // when
        val response = executeGetDoctors(restTemplate)

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
        executePostDoctors(restTemplate, givenCreateCommand)

        // when
        val response = executeGetDoctors(restTemplate)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.size).isEqualTo(1)
        assertEquals(response.body!![0], givenCreateCommand)
    }

    @Test
    internal fun shouldDeleteDoctorWithVisits() {

        // given
        val doctor =
            executePostDoctors(
                restTemplate,
                CreateOrUpdateDoctorCommand(name = "Joe", surname = "Doe", spec = "dent")
            ).body

        val patient = executePostPatient(restTemplate).body

        val visit: VisitView = VisitTestUtils.executePostVisit(
            restTemplate,
            CreateVisitCommand(
                LocalDateTime.of(2020, 12, 12, 12, 0),
                "location 123,  12-123 Test",
                doctor!!.id,
                patient!!.id
            )
        ).body!!

        // when
        val deleteResponse = executeDeleteDoctors(restTemplate, doctor.id)

        // then
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val getResponse = executeGetDoctors(restTemplate)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(0)
        assertThat(executeGetVisit(restTemplate, visit.id).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    internal fun shouldUpdateDoctor() {

        // given
        val doctor = executePostDoctors(
            restTemplate,
            CreateOrUpdateDoctorCommand(name = "Joe", surname = "Doe", spec = "dent")
        ).body
        val givenUpdateCommand = CreateOrUpdateDoctorCommand("Jim", "Bim", "surgeon")

        // when
        val updateResponse =
            executePutDoctors(restTemplate, givenUpdateCommand, doctor!!.id)

        // then
        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertEquals(updateResponse.body, givenUpdateCommand)

        val getResponse = executeGetDoctors(restTemplate)
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

    object DoctorTestUtils {

        fun executePostDoctors(
            restTemplate: TestRestTemplate,
            createDoctorCommand: CreateOrUpdateDoctorCommand =
                CreateOrUpdateDoctorCommand("Jim", "Bim", "surgeon")
        ): ResponseEntity<DoctorView> = restTemplate.exchange(
            "/doctors",
            HttpMethod.POST,
            HttpEntity(createDoctorCommand),
            DoctorView::class.java
        )
    }

    fun executePutDoctors(
        restTemplate: TestRestTemplate,
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

    fun executeDeleteDoctors(restTemplate: TestRestTemplate, id: Long): ResponseEntity<Void> {
        return restTemplate.exchange(
            "/doctors/$id",
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            Void::class.java
        )
    }

    fun executeGetDoctors(restTemplate: TestRestTemplate): ResponseEntity<List<DoctorView>> {
        return restTemplate.exchange(
            "/doctors",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            typeRef<List<DoctorView>>()
        )
    }

}
