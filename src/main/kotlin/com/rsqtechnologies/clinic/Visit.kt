package com.rsqtechnologies.clinic

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.transaction.Transactional

@Entity
data class Visit(
    val dateTime: LocalDateTime,
    val location: String,

    @JoinColumn(name = "doctor_id")
    @ManyToOne val doctor: Doctor,

    @JoinColumn(name = "patient_id")
    @ManyToOne val patient: Patient,

    @Version val version: Int? = 0,
    @Id
    @SequenceGenerator(
        name = "visit_id_seq",
        sequenceName = "visit_id_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "visit_id_seq") val id: Long? = null
) {

    companion object {
        fun from(createCommand: CreateVisitCommand, doctor: Doctor, patient: Patient) =
            Visit(
                dateTime = createCommand.dateTime,
                location = createCommand.location,
                patient = patient,
                doctor = doctor
            )
    }

    fun toDto(): VisitView =
        VisitView(
            dateTime = dateTime,
            location = location,
            doctor = doctor.toDto(),
            patient = patient.toDto(),
            id = id ?: 0
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Visit

        if (version != other.version) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version ?: 0
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Visit(dateTime=$dateTime, location='$location', doctor=$doctor, patientId=$patient, version=$version, id=$id)"
    }
}

interface VisitRepository : JpaRepository<Visit, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
                update Visit v set 
                    v.dateTime = :dateTime
                    where v.id = :id
                """
    )
    fun update(dateTime: LocalDateTime, id: Long)
    fun findAllByPatientId(patientId: Long): List<Visit>
}


@Service
class VisitService(
    private val doctorRepository: DoctorRepository,
    private val visitRepository: VisitRepository,
    private val patientRepository: PatientRepository
) {

    fun createVisit(createCommand: CreateVisitCommand): VisitView {

        val doctor = doctorRepository.findById(createCommand.doctorId)
            .orElseThrow { BadRequestException("Invalid doctor ID: " + createCommand.doctorId) }

        val patient = patientRepository.findById(createCommand.patientId)
            .orElseThrow { BadRequestException("Invalid patient ID: " + createCommand.patientId) }

        return visitRepository.save(Visit.from(createCommand = createCommand, doctor = doctor, patient = patient))
            .toDto()
    }

    fun updateVisitDateTime(dateTime: LocalDateTime, visitId: Long): Optional<VisitView> {
        visitRepository.update(dateTime, visitId)
        return visitRepository.findById(visitId).map(Visit::toDto)
    }

    fun deleteById(visitId: Long) {
        visitRepository.deleteById(visitId)
    }

    fun findVisits(patientId: Long): List<VisitView> {

        if (patientId > 0) {
            return visitRepository.findAllByPatientId(patientId).map(Visit::toDto)
        }
        return visitRepository.findAll().map(Visit::toDto)
    }

    fun findOneById(visitId: Long): Optional<VisitView> = visitRepository.findById(visitId).map(Visit::toDto)
}

@RestController
@RequestMapping("/visits")
class VisitController(private val visitService: VisitService) {

    @PutMapping("/{visitId}/dateTime")
    fun updateVisitDateTime(
        @PathVariable visitId: Long,
        @RequestBody updateCommand: UpdateVisitDateTimeCommand
    ) = ResponseEntity.of(visitService.updateVisitDateTime(updateCommand.dateTime, visitId))

    @DeleteMapping("/{visitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteVisit(@PathVariable visitId: Long) = visitService.deleteById(visitId)

    @PostMapping
    fun createVisit(@RequestBody createCommand: CreateVisitCommand) = visitService.createVisit(createCommand)

    @GetMapping
    fun findVisits(@RequestParam(defaultValue = "0") patientId: Long) = visitService.findVisits(patientId)

    @GetMapping("/{visitId}")
    fun findVisitById(@PathVariable visitId: Long) = ResponseEntity.of(visitService.findOneById(visitId))
}

data class CreateVisitCommand(
    val dateTime: LocalDateTime,
    val location: String,
    val doctorId: Long,
    val patientId: Long
)

data class UpdateVisitDateTimeCommand(
    val dateTime: LocalDateTime
)

data class VisitView(
    val dateTime: LocalDateTime,
    val location: String,
    val doctor: DoctorView,
    val patient: PatientView,
    val id: Long
)

