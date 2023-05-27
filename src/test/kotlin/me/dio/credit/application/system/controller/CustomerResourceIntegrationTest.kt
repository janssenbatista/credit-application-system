package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.TestBuilder.builderCustomerDto
import me.dio.credit.application.system.TestBuilder.builderCustomerUpdateDto
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.dto.request.CustomerUpdateDto
import me.dio.credit.application.system.entity.Customer
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CustomerResourceIntegrationTest {
    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mapper: ObjectMapper

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @AfterEach
    fun tearDown() = customerRepository.deleteAll()

    @Test
    fun `should create a customer and return 201 status`() {
        val customerDto: CustomerDto = builderCustomerDto()
        val valueAsString: String = mapper.writeValueAsString(customerDto)
        mockMvc.perform(
                MockMvcRequestBuilders.post(REQUEST_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(valueAsString)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Cami"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Cavalcante"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("28475934625"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("camila@email.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("1000.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("000000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua da Cami, 123"))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not save a customer with same email and return 409 status`() {
        customerRepository.save(builderCustomerDto().toEntity())
        val customerDto: CustomerDto = builderCustomerDto()
        testConflictFields(customerDto)
    }

    @Test
    fun `should not save a customer with same CPF and return 409 status`() {
        customerRepository.save(builderCustomerDto().toEntity())
        val customerDto: CustomerDto = builderCustomerDto(email = "customer@email.com")
        testConflictFields(customerDto)
    }


    @Test
    fun `should not save a customer with empty firstName and return 400 status`() {
        val customerDto: CustomerDto = builderCustomerDto(firstName = "")
        testEmptyFields(customerDto)
    }

    @Test
    fun `should not save a customer with empty lastname and return 400 status`() {
        val customerDto: CustomerDto = builderCustomerDto(lastName = "")
        testEmptyFields(customerDto)
    }

    @Test
    fun `should not save a customer with empty cpf and return 400 status`() {
        val customerDto: CustomerDto = builderCustomerDto(cpf = "")
        testEmptyFields(customerDto)
    }

    @Test
    fun `should not save a customer with empty email and return 400 status`() {
        val customerDto: CustomerDto = builderCustomerDto(email = "")
        testEmptyFields(customerDto)
    }

    @Test
    fun `should not save a customer with empty password and return 400 status`() {
        val customerDto: CustomerDto = builderCustomerDto(password = "")
        testEmptyFields(customerDto)
    }

    @Test
    fun `should find customer by id and return 200 status`() {
        val customer: Customer = customerRepository.save(builderCustomerDto().toEntity())
        mockMvc.perform(
                MockMvcRequestBuilders.get("$REQUEST_BASE_URL/${customer.id}")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Cami"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Cavalcante"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("28475934625"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("camila@email.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("1000.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("000000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua da Cami, 123"))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not find customer with invalid id and return 400 status`() {
        val invalidId = 2L
        mockMvc.perform(
                MockMvcRequestBuilders.get("$REQUEST_BASE_URL/$invalidId")
                        .accept(MediaType.APPLICATION_JSON)
        )
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
    fun `should delete customer by id and return 204 status`() {
        val customer: Customer = customerRepository.save(builderCustomerDto().toEntity())
        mockMvc.perform(
                MockMvcRequestBuilders.delete("$REQUEST_BASE_URL/${customer.id}")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not delete customer by id and return 400 status`() {
        val invalidId: Long = Random().nextLong()
        mockMvc.perform(
                MockMvcRequestBuilders.delete("$REQUEST_BASE_URL/${invalidId}")
                        .accept(MediaType.APPLICATION_JSON)
        )
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
    fun `should update a customer and return 200 status`() {
        val customer: Customer = customerRepository.save(builderCustomerDto().toEntity())
        val customerUpdateDto: CustomerUpdateDto = builderCustomerUpdateDto()
        val valueAsString: String = mapper.writeValueAsString(customerUpdateDto)
        mockMvc.perform(
                MockMvcRequestBuilders.patch(REQUEST_BASE_URL)
                        .param("customerId", customer.id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(valueAsString)
        )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("CamiUpdate"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("CavalcanteUpdate"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("28475934625"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("camila@email.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("5000.0"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("45656"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Rua Updated"))
                //.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not update a customer with invalid id and return 400 status`() {
        val invalidId: Long = Random().nextLong()
        val customerUpdateDto: CustomerUpdateDto = builderCustomerUpdateDto()
        val valueAsString: String = mapper.writeValueAsString(customerUpdateDto)
        mockMvc.perform(
                MockMvcRequestBuilders.patch("$REQUEST_BASE_URL?customerId=$invalidId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(valueAsString)
        )
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

    private fun testEmptyFields(customerDto: CustomerDto) {
        val valueAsString: String = mapper.writeValueAsString(customerDto)
        mockMvc.perform(
                MockMvcRequestBuilders.post(REQUEST_BASE_URL)
                        .content(valueAsString)
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

    private fun testConflictFields(customerDto: CustomerDto) {
        val valueAsString: String = mapper.writeValueAsString(customerDto)
        mockMvc.perform(
                MockMvcRequestBuilders.post(REQUEST_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(valueAsString)
        )
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Conflict: User already exists!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
                .andDo(MockMvcResultHandlers.print())
    }

    companion object {
        const val REQUEST_BASE_URL: String = "/api/customers"
    }

}