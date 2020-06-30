package com.rsqtechnologies.clinic

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*

internal class VisitServiceTest {

    private val visitRepository = Mockito.mock(VisitRepository::class.java)
    private val doctorRepository = Mockito.mock(DoctorRepository::class.java)
    private val patientRepository = Mockito.mock(PatientRepository::class.java)
    private val visitService = VisitService(doctorRepository, visitRepository, patientRepository)

    @AfterEach
    internal fun tearDown() {
        Mockito.reset(visitRepository, doctorRepository, patientRepository)
    }

    @Test
    internal fun shouldThrowInvalidDoctorIdWhenDoctorNotFound() {

        // given
        val givenNotExistingDoctorId = 1L
        Mockito.`when`(doctorRepository.findById(givenNotExistingDoctorId)).thenReturn(Optional.empty())
        val givenCreateCommand = CreateVisitCommand(
            LocalDateTime.now(),
            "test 123, 12_123 Test",
            givenNotExistingDoctorId,
            2
        )

        // then
        assertThrows<BadRequestException>("Invalid doctor ID: $givenNotExistingDoctorId") {
            // when
            visitService.createVisit(
                givenCreateCommand
            )
        }
    }

    @Test
    internal fun shouldThrowInvalidPatientIdWhenPatientNotFound() {

        // given
        val givenExistingDoctorId = 1L
        Mockito.`when`(doctorRepository.findById(givenExistingDoctorId))
            .thenReturn(Optional.of(Doctor("", "", "", setOf())))

        val givenNotExistingPatientId = 3L
        Mockito.`when`(patientRepository.findById(givenNotExistingPatientId))
            .thenReturn(Optional.empty())

        val givenCreateCommand = CreateVisitCommand(
            LocalDateTime.now(),
            "test 123, 12_123 Test",
            givenExistingDoctorId,
            givenNotExistingPatientId
        )

        // then
        assertThrows<BadRequestException>("Invalid patient ID: $givenNotExistingPatientId") {
            // when
            visitService.createVisit(
                givenCreateCommand
            )
        }
    }
}