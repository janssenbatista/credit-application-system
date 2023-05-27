package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import me.dio.credit.application.system.TestBuilder.buildCustomer
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.exception.CustomerAlreadyExistsException
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

    @MockK
    lateinit var customerRepository: CustomerRepository

    @InjectMockKs
    lateinit var customerService: CustomerService

    @Test
    fun `should create customer`() {
        val fakeCustomer: Customer = buildCustomer(1L)
        every { customerRepository.findByEmail(fakeCustomer.email) } returns Optional.empty()
        every { customerRepository.findByCpf(fakeCustomer.cpf) } returns Optional.empty()
        every { customerRepository.save(any()) } returns fakeCustomer
        val actual: Customer = customerService.save(fakeCustomer)
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isSameAs(fakeCustomer)
        verify(exactly = 1) { customerRepository.save(fakeCustomer) }
    }

    @Test
    fun `should not be able to create a customer with existing email`() {
        val fakeCustomer = buildCustomer(id = null)
        every { customerRepository.findByEmail(fakeCustomer.email) } returns Optional.of(Customer())
        Assertions.assertThatExceptionOfType(CustomerAlreadyExistsException::class.java).isThrownBy {
            customerService.save(fakeCustomer)
        }.withMessage("User already exists!")
        verify(exactly = 0) { customerService.save(any()) }
    }

    @Test
    fun `should not be able to create a customer with existing cpf`() {
        val fakeCustomer = buildCustomer(id = null)
        every { customerRepository.findByEmail(fakeCustomer.email) } returns Optional.empty()
        every { customerRepository.findByCpf(fakeCustomer.cpf) } returns Optional.of(Customer())
        Assertions.assertThatExceptionOfType(CustomerAlreadyExistsException::class.java).isThrownBy {
            customerService.save(fakeCustomer)
        }.withMessage("User already exists!")
        verify(exactly = 0) { customerService.save(any()) }
    }

    @Test
    fun `should find customer by id`() {
        val fakeId: Long = Random().nextLong()
        val fakeCustomer: Customer = buildCustomer(id = fakeId)
        every { customerRepository.findById(fakeId) } returns Optional.of(fakeCustomer)
        val actual: Customer = customerService.findById(fakeId)
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isExactlyInstanceOf(Customer::class.java)
        Assertions.assertThat(actual).isSameAs(fakeCustomer)
        verify(exactly = 1) { customerRepository.findById(fakeId) }
    }

    @Test
    fun `should not find customer by invalid id and throw BusinessException`() {
        val fakeId: Long = Random().nextLong()
        every { customerRepository.findById(fakeId) } returns Optional.empty()
        Assertions.assertThatExceptionOfType(BusinessException::class.java)
                .isThrownBy { customerService.findById(fakeId) }
                .withMessage("Id $fakeId not found")
        verify(exactly = 1) { customerRepository.findById(fakeId) }
    }

    @Test
    fun `should delete customer by id`() {
        val fakeId: Long = Random().nextLong()
        val fakeCustomer: Customer = buildCustomer(id = fakeId)
        every { customerRepository.findById(fakeId) } returns Optional.of(fakeCustomer)
        every { customerRepository.delete(fakeCustomer) } just runs
        customerService.delete(fakeId)
        verify(exactly = 1) { customerRepository.findById(fakeId) }
        verify(exactly = 1) { customerRepository.delete(fakeCustomer) }
    }

}