package me.dio.credit.application.system.repository

import me.dio.credit.application.system.TestBuilder.buildCustomer
import me.dio.credit.application.system.entity.Customer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryIntegrationTest {

    private lateinit var customer: Customer

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @BeforeEach
    fun setup() {
        customer = customerRepository.save(buildCustomer(1))
    }

    @AfterEach
    fun tearDown() {
        customerRepository.deleteAll()
    }

    @Test
    fun `should be able to find a user with the email passed`() {
        Assertions.assertNotNull(customerRepository.findByEmail(customer.email))
    }

    @Test
    fun `should be able to find a user with the cpf passed`() {
        Assertions.assertNotNull(customerRepository.findByCpf(customer.cpf))
    }

    @Test
    fun `should not be able to find a user with the email passed`() {
        customerRepository.delete(customer)
        Assertions.assertEquals(Optional.empty<Customer>(), customerRepository.findByEmail(customer.email))
    }

    @Test
    fun `should not be able to find a user with the cpf passed`() {
        customerRepository.delete(customer)
        Assertions.assertEquals(Optional.empty<Customer>(), customerRepository.findByCpf(customer.cpf))
    }

}