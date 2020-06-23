package com.rsqtechnologies.clinic

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.persistence.*
import javax.transaction.Transactional
import javax.websocket.server.PathParam

@Entity
data class Visit(
    val dateTime: LocalDateTime,
    val location: String,
    val doctorId: Long,
    val patientId: Long,
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
        fun from(createCommand: CreateVisitCommand) =
            Visit(
                dateTime = createCommand.dateTime,
                location = createCommand.location,
                doctorId = createCommand.doctorId,
                patientId = createCommand.patientId
            )
    }

    fun toDto(): VisitView =
        VisitView(dateTime = dateTime, location = location, doctorId = doctorId, patientId = patientId, id = id ?: 0)
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

@RestController
@RequestMapping("/visits")
class VisitController(private val repository: VisitRepository) {

    @PutMapping("/{visitId}/dateTime")
    fun updateVisitTime(
        @PathVariable visitId: Long,
        @RequestBody updateCommand: UpdateVisitDateTimeCommand
    ): ResponseEntity<VisitView> {

        repository.update(updateCommand.dateTime, visitId)

        return ResponseEntity.of(repository.findById(visitId).map(Visit::toDto))
    }

    @DeleteMapping("/{visitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteVisit(@PathVariable visitId: Long) = repository.deleteById(visitId)

    @PostMapping
    fun createVisit(@RequestBody createCommand: CreateVisitCommand) =
        repository.save(Visit.from(createCommand)).toDto()

    @GetMapping
    fun getVisit(@RequestParam(defaultValue = "0") patientId: Long): List<VisitView> {

        if (patientId > 0) {
            return repository.findAllByPatientId(patientId).map(Visit::toDto)
        }
        return repository.findAll().map(Visit::toDto)
    }

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
    val doctorId: Long,
    val patientId: Long,
    val id: Long
)

