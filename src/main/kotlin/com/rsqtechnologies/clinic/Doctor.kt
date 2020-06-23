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
    val name: String,
    val surname: String,
    val spec: String,
    @Version val version: Int? = 0,
    @Id
    @SequenceGenerator(
        name = "doctor_id_seq",
        sequenceName = "doctor_id_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doctor_id_seq") val id: Long? = null
) {
    companion object {
        fun from(createCommand: CreateOrUpdateDoctorCommand) =
            Doctor(name = createCommand.name, surname = createCommand.surname, spec = createCommand.spec)
    }

    fun toDto(): DoctorView = DoctorView(name, surname, spec, id ?: 0)
}

interface DoctorRepository : JpaRepository<Doctor, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
                update Doctor d set 
                    d.name = :name, 
                    d.surname = :surname, 
                    d.spec = :spec 
                    where d.id = :id
                """
    )
    fun update(name: String, surname: String, spec: String, id: Long)
}

@RestController
@RequestMapping("/doctors")
class DoctorController(private val repository: DoctorRepository) {

    @PutMapping("/{doctorId}")
    fun updateDoctor(
        @PathVariable doctorId: Long,
        @RequestBody updateCommand: CreateOrUpdateDoctorCommand
    ): ResponseEntity<DoctorView> {

        repository.update(updateCommand.name, updateCommand.surname, updateCommand.spec, doctorId)

        return ResponseEntity.of(repository.findById(doctorId).map(Doctor::toDto))
    }

    @DeleteMapping("/{doctorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDoctor(@PathVariable doctorId: Long) = repository.deleteById(doctorId)

    @PostMapping
    fun createDoctor(@RequestBody createCommand: CreateOrUpdateDoctorCommand) =
        repository.save(Doctor.from(createCommand)).toDto()

    @GetMapping
    fun getDoctors() =
        repository.findAll().map(Doctor::toDto)

}

data class CreateOrUpdateDoctorCommand(val name: String, val surname: String, val spec: String)
data class DoctorView(val name: String, val surname: String, val spec: String, val id: Long)

