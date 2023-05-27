package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import me.dio.credit.application.system.TestBuilder.buildCredit
import me.dio.credit.application.system.TestBuilder.buildCustomer
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.util.*
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class CreditServiceUnitTest {

    private val creditRepository: CreditRepository = mockk<CreditRepository>()
    private val customerService: CustomerService = mockk<CustomerService>()
    private val creditService: CreditService = CreditService(creditRepository, customerService)
    private lateinit var credit: Credit

    @BeforeEach
    fun setup() {
        credit = buildCredit()
    }

    @Test
    @DisplayName("Save credit")
    fun `should save a credit`() {
        every { customerService.findById(credit.customer?.id!!) } returns buildCustomer(1)
        every { creditRepository.save(credit) } returns credit
        creditService.save(credit)
        verify(exactly = 1) { customerService.findById(credit.customer?.id!!) }
        verify(exactly = 1) { creditRepository.save(credit) }
    }

    @Test
    fun `should throw BusinessException when first installment month is equal or more than 3`() {
        credit.dayFirstInstallment = LocalDate.now().plusMonths(3)
        assertThrows(BusinessException::class.java) {
            creditService.save(credit)
        }
        verify(exactly = 0) { creditRepository.save(credit) }
    }

    @Test
    fun `should find all credits by customer id`() {
        val customer1 = buildCustomer(id = 1)
        val customer2 = buildCustomer(id = 2)
        val credits = listOf(buildCredit(customer1), buildCredit(customer1), buildCredit(customer2))
        every { creditRepository.findAllByCustomerId(customer1.id!!) } returns credits
                .filter { credit: Credit -> credit.customer?.id == customer1.id }
        val creditsList = creditService.findAllByCustomer(customer1.id!!)
        assertNotEquals(credits, creditsList)
        assertEquals(2, creditsList.size)
    }

    @Test
    fun `should find a credit by credit code and customer id`() {
        val customer = buildCustomer(id = 1)
        val credit = buildCredit(customer)
        every { creditRepository.findByCreditCode(credit.creditCode) } returns credit
        val creditFound = creditService.findByCreditCode(customer.id!!, credit.creditCode)

        assertEquals(creditFound.id, credit.id)
        assertEquals(creditFound.status, credit.status)
        assertEquals(creditFound.creditCode, credit.creditCode)
        assertEquals(creditFound.creditValue, credit.creditValue)
        assertEquals(creditFound.dayFirstInstallment, credit.dayFirstInstallment)
        assertEquals(creditFound.numberOfInstallments, credit.numberOfInstallments)
        assertEquals(creditFound.customer, credit.customer)
    }

    @Test
    fun `should throw BusinessException when credit code does not exists`() {
        val creditId = UUID.randomUUID()
        every { creditRepository.findByCreditCode(creditId) } returns null
        assertThrows(BusinessException::class.java) {
            creditService.findByCreditCode(Random.nextLong(), creditId)
        }
    }

    @Test
    fun `should throw IllegalArgumentException when credit customer id is not equal customer id`() {
        val customer1 = buildCustomer(id = 1)
        val customer2 = buildCustomer(id = 2)
        val credit = buildCredit(customer2)
        every { creditRepository.findByCreditCode(credit.creditCode) } returns credit
        assertThrows(IllegalArgumentException::class.java) {
            creditService.findByCreditCode(customer1.id!!, credit.creditCode)
        }
    }
}