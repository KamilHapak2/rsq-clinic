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
data class Patient(
    val name: String,
    val surname: String,
    val address: String,
    @Version val version: Int? = 0,
    @Id
    @SequenceGenerator(
        name = "patient_id_seq",
        sequenceName = "patient_id_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_id_seq") val id: Long? = null
) {
    companion object {
        fun from(createCommand: CreateOrUpdatePatientCommand) =
            Patient(name = createCommand.name, surname = createCommand.surname, address = createCommand.address)
    }

    fun toDto(): PatientView = PatientView(name, surname, address, id ?: 0)
}

interface PatientRepository : JpaRepository<Patient, Long> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
                update Patient p set 
                    p.name = :name, 
                    p.surname = :surname, 
                    p.address = :address 
                    where p.id = :id
                """
    )
    fun update(name: String, surname: String, address: String, id: Long)
}

@RestController
@RequestMapping("/patients")
class PatientController(private val repository: PatientRepository) {

    @PutMapping("/{patientId}")
    fun updatePatient(
        @PathVariable patientId: Long,
        @RequestBody updateCommand: CreateOrUpdatePatientCommand
    ): ResponseEntity<PatientView> {

        repository.update(updateCommand.name, updateCommand.surname, updateCommand.address, patientId)

        return ResponseEntity.of(repository.findById(patientId).map(Patient::toDto))
    }

    @DeleteMapping("/{patientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePatient(@PathVariable patientId: Long) = repository.deleteById(patientId)

    @PostMapping
    fun createPatient(@RequestBody createCommand: CreateOrUpdatePatientCommand) =
        repository.save(Patient.from(createCommand)).toDto()

    @GetMapping
    fun getPatients() =
        repository.findAll().map(Patient::toDto)

}

data class CreateOrUpdatePatientCommand(val name: String, val surname: String, val address: String)
data class PatientView(val name: String, val surname: String, val address: String, val id: Long)

