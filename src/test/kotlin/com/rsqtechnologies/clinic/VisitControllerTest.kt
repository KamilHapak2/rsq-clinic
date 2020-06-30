package com.rsqtechnologies.clinic

import com.rsqtechnologies.clinic.DoctorControllerTest.DoctorTestUtils.executePostDoctors
import com.rsqtechnologies.clinic.PatientControllerTest.PatientTestUtils.executePostPatient
import com.rsqtechnologies.clinic.VisitControllerTest.VisitTestUtils.executeDeleteVisit
import com.rsqtechnologies.clinic.VisitControllerTest.VisitTestUtils.executeGetVisits
import com.rsqtechnologies.clinic.VisitControllerTest.VisitTestUtils.executePostVisit
import com.rsqtechnologies.clinic.VisitControllerTest.VisitTestUtils.executePutVisitDateTime
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
class VisitControllerTest {

    @BeforeEach
    internal fun setUp() {
        ClinicTestContainer.createDropDatabase()
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    internal fun shouldGetVisits() {

        // given
        val givenPatientId = executePostPatient(restTemplate).body!!.id
        val givenDoctorId = executePostDoctors(restTemplate).body!!.id
        executePostVisit(
            restTemplate,
            CreateVisitCommand(
                dateTime = LocalDateTime.of(2020, 1, 1, 12, 0),
                location = "Health 123, 12-222 Hospital",
                patientId = givenPatientId,
                doctorId = givenDoctorId
            )
        )

        executePostVisit(
            restTemplate,
            CreateVisitCommand(
                dateTime = LocalDateTime.of(2021, 12, 2, 10, 30),
                location = "Amazing 22, 01-001 York",
                patientId = givenPatientId,
                doctorId = givenDoctorId
            )
        )

        // when
        val response = executeGetVisits(restTemplate, givenPatientId)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.size).isEqualTo(2)
    }

    @Test
    internal fun shouldCreateVisit() {

        // given
        val givenPatient = executePostPatient(restTemplate).body
        val givenDoctor = executePostDoctors(restTemplate).body

        val givenCreateCommand = CreateVisitCommand(
            dateTime = LocalDateTime.of(2020, 1, 1, 12, 0),
            location = "Health 123, 12-222 Hospital",
            patientId = givenPatient!!.id,
            doctorId = givenDoctor!!.id
        )

        // when
        executePostVisit(
            restTemplate,
            givenCreateCommand
        )

        // then
        val response = executeGetVisits(restTemplate)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.size).isEqualTo(1)
        assertEquals(response.body!![0], givenCreateCommand, givenDoctor, givenPatient)
    }

    @Test
    internal fun shouldDeleteVisit() {

        // given
        val visit = executePostVisit(
            restTemplate,
            CreateVisitCommand(
                dateTime = LocalDateTime.of(2020, 1, 1, 12, 0),
                location = "Health 123, 12-222 Hospital",
                patientId = executePostPatient(restTemplate).body!!.id,
                doctorId = executePostDoctors(restTemplate).body!!.id
            )
        ).body

        // when
        val deleteResponse = executeDeleteVisit(restTemplate, visit!!.id)

        // then
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val getResponse = executeGetVisits(restTemplate)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(0)
    }

    @Test
    internal fun shouldUpdateVisit() {

        val givenDoctor = executePostDoctors(restTemplate).body
        val givenPatient = executePostPatient(restTemplate).body
        val givenDateTime = LocalDateTime.of(2020, 1, 1, 12, 0)
        val expectedDateTime = LocalDateTime.of(2020, 1, 2, 12, 0)
        val givenLocation = "Health 123, 12-222 Hospital"

        val visit = executePostVisit(
            restTemplate,
            CreateVisitCommand(
                dateTime = givenDateTime,
                location = givenLocation,
                patientId = givenPatient!!.id,
                doctorId = givenDoctor!!.id
            )
        ).body

        // when
        val updateResponse =
            executePutVisitDateTime(
                restTemplate,
                UpdateVisitDateTimeCommand(expectedDateTime),
                visit!!.id
            )

        // then
        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(updateResponse.body).isNotNull
        assertThat(updateResponse.body!!.dateTime).isEqualTo(expectedDateTime)
        assertThat(updateResponse.body!!.location).isEqualTo(givenLocation)
        assertThat(updateResponse.body!!.patient.id).isEqualTo(givenPatient.id)
        assertThat(updateResponse.body!!.doctor.id).isEqualTo(givenDoctor.id)

        val getResponse = executeGetVisits(restTemplate)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(1)
        assertThat(getResponse.body!![0].dateTime).isEqualTo(expectedDateTime)
        assertThat(getResponse.body!![0].location).isEqualTo(givenLocation)
        assertThat(getResponse.body!![0].patient.id).isEqualTo(givenPatient.id)
        assertThat(getResponse.body!![0].doctor.id).isEqualTo(givenDoctor.id)
    }

    private fun assertEquals(
        actualVisit: VisitView?,
        expectedVisit: CreateVisitCommand,
        expectedDoctor: DoctorView,
        expectedPatient: PatientView
    ) {
        assertThat(actualVisit).isNotNull
        assertThat(actualVisit!!.dateTime).isEqualTo(expectedVisit.dateTime)
        assertThat(actualVisit.location).isEqualTo(expectedVisit.location)
        assertThat(actualVisit.doctor.id).isEqualTo(expectedVisit.doctorId)
        assertThat(actualVisit.patient.id).isEqualTo(expectedVisit.patientId)

        assertThat(actualVisit.doctor.id).isEqualTo(expectedDoctor.id)
        assertThat(actualVisit.doctor.name).isEqualTo(expectedDoctor.name)
        assertThat(actualVisit.doctor.surname).isEqualTo(expectedDoctor.surname)
        assertThat(actualVisit.doctor.spec).isEqualTo(expectedDoctor.spec)

        assertThat(actualVisit.patient.id).isEqualTo(expectedPatient.id)
        assertThat(actualVisit.patient.name).isEqualTo(expectedPatient.name)
        assertThat(actualVisit.patient.surname).isEqualTo(expectedPatient.surname)
        assertThat(actualVisit.patient.address).isEqualTo(expectedPatient.address)

    }

    object VisitTestUtils {

        fun executeGetVisit(
            restTemplate: TestRestTemplate,
            visitId: Long
        ): ResponseEntity<VisitView> = restTemplate.exchange(
            "/visits/$visitId",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            VisitView::class.java
        )

        fun executeGetVisits(
            restTemplate: TestRestTemplate,
            patientId: Long = 0
        ): ResponseEntity<List<VisitView>> = restTemplate.exchange(
            "/visits?patientId=$patientId",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            typeRef<List<VisitView>>()
        )

        fun executePostVisit(
            restTemplate: TestRestTemplate,
            createVisitCommand: CreateVisitCommand
        ): ResponseEntity<VisitView> = restTemplate.exchange(
            "/visits",
            HttpMethod.POST,
            HttpEntity(createVisitCommand),
            VisitView::class.java
        )

        fun executePutVisitDateTime(
            restTemplate: TestRestTemplate,
            updateVisitCommand: UpdateVisitDateTimeCommand,
            id: Long
        ): ResponseEntity<VisitView> = restTemplate.exchange(
            "/visits/$id/dateTime",
            HttpMethod.PUT,
            HttpEntity(updateVisitCommand),
            VisitView::class.java
        )

        fun executeDeleteVisit(restTemplate: TestRestTemplate, id: Long): ResponseEntity<Void> {
            return restTemplate.exchange(
                "/visits/$id",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void::class.java
            )
        }
    }
}
