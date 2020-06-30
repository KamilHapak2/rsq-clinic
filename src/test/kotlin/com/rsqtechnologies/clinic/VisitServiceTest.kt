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
    private val visitService = VisitService(doctorRepository, visitRepository)

    @AfterEach
    internal fun tearDown() {
        Mockito.reset(visitRepository, doctorRepository)
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

        assertThrows<InvalidDoctorIdException>("Invalid doctor ID: $givenNotExistingDoctorId") {
            visitService.createVisit(
                givenCreateCommand
            )
        }
    }
}