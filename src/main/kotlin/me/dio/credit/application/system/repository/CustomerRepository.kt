package me.dio.credit.application.system.repository

import me.dio.credit.application.system.entity.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CustomerRepository : JpaRepository<Customer, Long> {
    fun findByEmail(email: String): Optional<Customer>
    fun findByCpf(cpf: String): Optional<Customer>
}