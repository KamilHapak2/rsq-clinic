package com.rsqtechnologies.clinic

import com.rsqtechnologies.clinic.DoctorControllerTest.DoctorTestUtils.executePostDoctors
import com.rsqtechnologies.clinic.PatientControllerTest.PatientTestUtils.executePostPatient
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
        val givenPatientId = createPatient()
        executePostVisit(
            CreateVisitCommand(
                dateTime = LocalDateTime.of(2020, 1, 1, 12, 0),
                location = "Health 123, 12-222 Hospital",
                patientId = givenPatientId,
                doctorId = createDoctor()
            )
        )

        executePostVisit(
            CreateVisitCommand(
                dateTime = LocalDateTime.of(2021, 12, 2, 10, 30),
                location = "Amazing 22, 01-001 York",
                patientId = givenPatientId,
                doctorId = createDoctor()
            )
        )

        // when
        val response = executeGetVisits(patientId = givenPatientId)

        // then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.size).isEqualTo(2)
    }

    @Test
    internal fun shouldCreateVisit() {

        // given

        val givenCreateCommand = CreateVisitCommand(
            dateTime = LocalDateTime.of(2020, 1, 1, 12, 0),
            location = "Health 123, 12-222 Hospital",
            patientId = createPatient(),
            doctorId = createDoctor()
        )

        // when
        executePostVisit(
            givenCreateCommand
        )

        // then
        val response = executeGetVisits()
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.size).isEqualTo(1)
        assertEquals(response.body!![0], givenCreateCommand)
    }

    @Test
    internal fun shouldDeleteVisit() {

        // given
        val visit = executePostVisit(
            CreateVisitCommand(
                dateTime = LocalDateTime.of(2020, 1, 1, 12, 0),
                location = "Health 123, 12-222 Hospital",
                patientId = createPatient(),
                doctorId = createDoctor()
            )
        ).body

        // when
        val deleteResponse = executeDeleteVisit(visit!!.id)

        // then
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val getResponse = executeGetVisits()
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(0)
    }

    @Test
    internal fun shouldUpdatePatient() {

        val givenDoctorId = createDoctor()
        val givenPatientId = createPatient()
        val givenDateTime = LocalDateTime.of(2020, 1, 1, 12, 0)
        val expectedDateTime = LocalDateTime.of(2020, 1, 2, 12, 0)
        val givenLocation = "Health 123, 12-222 Hospital"

        val visit = executePostVisit(
            CreateVisitCommand(
                dateTime = givenDateTime,
                location = givenLocation,
                patientId = givenPatientId,
                doctorId = givenDoctorId
            )
        ).body

        // when
        val updateResponse =
            executePatchVisitDateTime(
                UpdateVisitDateTimeCommand(expectedDateTime),
                visit!!.id
            )

        // then
        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(updateResponse.body).isNotNull
        assertThat(updateResponse.body!!.dateTime).isEqualTo(expectedDateTime)
        assertThat(updateResponse.body!!.location).isEqualTo(givenLocation)
        assertThat(updateResponse.body!!.patientId).isEqualTo(givenPatientId)
        assertThat(updateResponse.body!!.doctorId).isEqualTo(givenDoctorId)

        val getResponse = executeGetVisits()
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body).isNotNull
        assertThat(getResponse.body!!.size).isEqualTo(1)
        assertThat(updateResponse.body!!.dateTime).isEqualTo(expectedDateTime)
        assertThat(updateResponse.body!!.location).isEqualTo(givenLocation)
        assertThat(updateResponse.body!!.patientId).isEqualTo(givenPatientId)
        assertThat(updateResponse.body!!.doctorId).isEqualTo(givenDoctorId)
    }

    private fun assertEquals(
        actualVisit: VisitView?,
        expectedVisit: CreateVisitCommand
    ) {
        assertThat(actualVisit).isNotNull
        assertThat(actualVisit!!.dateTime).isEqualTo(expectedVisit.dateTime)
        assertThat(actualVisit.location).isEqualTo(expectedVisit.location)
        assertThat(actualVisit.doctorId).isEqualTo(expectedVisit.doctorId)
        assertThat(actualVisit.patientId).isEqualTo(expectedVisit.patientId)
    }

    private fun executePostVisit(createVisitCommand: CreateVisitCommand): ResponseEntity<VisitView> {
        return restTemplate.exchange(
            "/visits",
            HttpMethod.POST,
            HttpEntity(createVisitCommand),
            VisitView::class.java
        )
    }

    private fun executePatchVisitDateTime(
        updateVisitCommand: UpdateVisitDateTimeCommand,
        id: Long
    ): ResponseEntity<VisitView> {
        return restTemplate.exchange(
            "/visits/$id/dateTime",
            HttpMethod.PUT,
            HttpEntity(updateVisitCommand),
            VisitView::class.java
        )
    }

    private fun executeDeleteVisit(id: Long): ResponseEntity<Void> {
        return restTemplate.exchange(
            "/visits/$id",
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            Void::class.java
        )
    }

    private fun executeGetVisits(): ResponseEntity<List<VisitView>> {
        return restTemplate.exchange(
            "/visits",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            typeRef<List<VisitView>>()
        )
    }

    private fun executeGetVisits(patientId: Long): ResponseEntity<List<VisitView>> {
        return restTemplate.exchange(
            "/visits?patientId=$patientId",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            typeRef<List<VisitView>>()
        )
    }

    private fun createDoctor() =
        executePostDoctors(
            restTemplate, CreateOrUpdateDoctorCommand(
                "Jim",
                "Bim",
                "surgeon"
            )
        ).body!!.id

    private fun createPatient() =
        executePostPatient(
            restTemplate, CreateOrUpdatePatientCommand(
                "Jim",
                "Bim",
                "Jin 123, 12-123 Bim"
            )
        ).body!!.id
}
