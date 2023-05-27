package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.TestBuilder.buildCredit
import me.dio.credit.application.system.TestBuilder.buildCreditDto
import me.dio.credit.application.system.TestBuilder.buildCustomer
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.random.Random

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceIntegrationTest {

    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    private lateinit var customer: Customer

    @BeforeEach
    fun setup() {
        customer = customerRepository.save(buildCustomer(1))
    }

    @AfterEach
    fun tearDown() {
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    // Save saveCredit()

    @Test
    fun `should create a credit and return status code 201`() {
        val creditDto = buildCreditDto(customerId = customer.id!!)
        val creditDtoAsString = mapper.writeValueAsString(creditDto)
        mockMvc.perform(post(REQUEST_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditDtoAsString))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated)
    }

    @Test
    fun `should return status 400 when customer id is invalid`() {
        val creditDto = buildCreditDto(customerId = Random.nextLong())
        mockMvc.perform(post(REQUEST_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(creditDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.exception")
                                .value("class me.dio.credit.application.system.exception.BusinessException")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should return status code 400 when credit value is missing`() {
        val creditDto = buildCreditDto()
        val creditJsonObject = CreditJsonObject(
                creditValue = "",
                dayFirstOfInstallment = creditDto.dayFirstOfInstallment,
                numberOfInstallments = creditDto.numberOfInstallments,
                customerId = creditDto.customerId
        )
        testEmptyFields(mapper.writeValueAsString(creditJsonObject))
    }

    @Test
    fun `should status code 400 when credit value is less then or equal to 0`() {
        var creditDto = buildCreditDto(creditValue = BigDecimal.valueOf(-1))
        testInvalidFields(mapper.writeValueAsString(creditDto))
        creditDto = buildCreditDto(creditValue = BigDecimal.ZERO)
        testInvalidFields(mapper.writeValueAsString(creditDto))
    }

    @Test
    fun `should return status code 400 when day first of installment value is missing`() {
        val creditDto = buildCreditDto()
        val creditJsonObject = CreditJsonObject(
                creditValue = creditDto.creditValue,
                dayFirstOfInstallment = "",
                numberOfInstallments = creditDto.numberOfInstallments,
                customerId = creditDto.customerId
        )
        testEmptyFields(mapper.writeValueAsString(creditJsonObject))
    }

    @Test
    fun `should return status code 400 when day first of installment is before now`() {
        val creditDto = buildCreditDto(dayFirstOfInstallment = LocalDate.now().minusDays(1))
        testInvalidFields(mapper.writeValueAsString(creditDto))
    }

    @Test
    fun `should return status code 400 when day number of installments value is missing`() {
        val creditDto = buildCreditDto()
        val creditJsonObject = CreditJsonObject(
                creditValue = creditDto.creditValue,
                dayFirstOfInstallment = creditDto.dayFirstOfInstallment,
                numberOfInstallments = "",
                customerId = creditDto.customerId
        )
        testEmptyFields(mapper.writeValueAsString(creditJsonObject))
    }

    @Test
    fun `should return status code 400 when day number of installments is less then 1`() {
        val creditDto = buildCreditDto(numberOfInstallments = 0)
        testInvalidFields(mapper.writeValueAsString(creditDto))
    }

    @Test
    fun `should return status code 400 when day number of installments is more then 48`() {
        val creditDto = buildCreditDto(numberOfInstallments = 49)
        testInvalidFields(mapper.writeValueAsString(creditDto))
    }

    @Test
    fun `should return status code 400 when customer id value is missing`() {
        val creditDto = buildCreditDto()
        val creditJsonObject = CreditJsonObject(
                creditValue = creditDto.creditValue,
                dayFirstOfInstallment = creditDto.dayFirstOfInstallment,
                numberOfInstallments = creditDto.numberOfInstallments,
                customerId = ""
        )
        val json = mapper.writeValueAsString(creditJsonObject)
        testEmptyFields(json)
    }

    @Test
    fun `should return status code 400 when dayFirstInstallment is equal o more then 3`() {
        val creditDto = buildCreditDto(dayFirstOfInstallment = LocalDate.now().plusMonths(3))
        val creditDtoAsString = mapper.writeValueAsString(creditDto)
        mockMvc.perform(post(REQUEST_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditDtoAsString))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    // Test findAllByCustomerId()

    @Test
    fun `should find all credits by a customer id and return status 200`() {
        val credit1 = buildCredit(customer = customer)
        val credit2 = buildCredit(customer = customer)
        val customer2 = customerRepository.save(buildCustomer(
                cpf = Random.nextBytes(11).toString(),
                email = Random.nextBytes(11).toString()))
        val credit3 = buildCredit(customer = customer2)
        creditRepository.saveAll(listOf(credit1, credit2, credit3))

        val resultAction = mockMvc.perform(get(REQUEST_BASE_URL)
                .param("customerId", customer.id.toString())
                .accept(MediaType.APPLICATION_JSON))
        resultAction
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()
    }

    // Test findByCreditCode

    @Test
    fun `should find a credit by credit code and return status 200`() {
        val credit = buildCredit(customer = customer)
        creditRepository.save(credit)
        mockMvc.perform(get("$REQUEST_BASE_URL/${credit.creditCode}")
                .param("customerId", customer.id.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should return status code 400 when credit code is invalid`() {
        val credit = buildCredit(customer = customer)
        mockMvc.perform(get("$REQUEST_BASE_URL/${credit.creditCode}")
                .param("customerId", customer.id.toString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.exception")
                                .value("class me.dio.credit.application.system.exception.BusinessException")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should return status code 400 when credit customer id is not equal customer id`() {
        val credit = buildCredit(customer = customer)
        creditRepository.save(credit)
        mockMvc.perform(get("$REQUEST_BASE_URL/${credit.creditCode}")
                .param("customerId", Random.nextLong().toString()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.exception")
                                .value("class java.lang.IllegalArgumentException")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
    }


    private fun testEmptyFields(jsonString: String) {
        mockMvc.perform(
                post(REQUEST_BASE_URL)
                        .content(jsonString)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    private fun testInvalidFields(jsonString: String) {
        mockMvc.perform(
                post(REQUEST_BASE_URL)
                        .content(jsonString)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.exception")
                                .value("class org.springframework.web.bind.MethodArgumentNotValidException")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
    }

    companion object {
        const val REQUEST_BASE_URL = "/api/credits"

        data class CreditJsonObject(
                val creditValue: Any,
                val dayFirstOfInstallment: Any,
                val numberOfInstallments: Any,
                val customerId: Any
        )

    }
}