package com.rsqtechnologies.clinic

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.persistence.*
import org.springframework.data.jpa.repository.Query
import org.springframework.http.ResponseEntity
import javax.transaction.Transactional


@Entity
data class Doctor(
    @Id
    @SequenceGenerator(
        name = "doctor_id_seq",
        sequenceName = "doctor_id_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doctor_id_seq") val id: Long? = null,
    val name: String

) {
    companion object {
        fun from(createCommand: CreateDoctorCommand) = Doctor(name = createCommand.name)
    }

    fun toDto(): DoctorView = DoctorView(name, id ?: 0)
}

interface DoctorRepository : JpaRepository<Doctor, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Doctor d set d.name = :name where d.id = :id")
    fun update(name: String, id: Long)
}

@RestController
@RequestMapping("/doctors")
class DoctorController(private val repository: DoctorRepository) {

    @PutMapping("/{doctorId}")
    fun updateDoctor(
        @PathVariable doctorId: Long,
        @RequestBody createCommand: CreateDoctorCommand
    ): ResponseEntity<DoctorView> {

        repository.update(createCommand.name, doctorId)

        return ResponseEntity.of(repository.findById(doctorId).map(Doctor::toDto))
    }

    @DeleteMapping("/{doctorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDoctor(@PathVariable doctorId: Long) = repository.deleteById(doctorId)

    @PostMapping
    fun createDoctor(@RequestBody createCommand: CreateDoctorCommand) =
        repository.save(Doctor.from(createCommand)).toDto()

    @GetMapping
    fun getDoctors() =
        repository.findAll().map(Doctor::toDto)

}

data class CreateDoctorCommand(val name: String)
data class DoctorView(val name: String, val id: Long)

