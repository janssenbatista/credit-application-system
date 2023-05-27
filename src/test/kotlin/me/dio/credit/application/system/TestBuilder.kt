package me.dio.credit.application.system

import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.dto.request.CustomerUpdateDto
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enummeration.Status
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

object TestBuilder {
    fun builderCustomerDto(
            firstName: String = "Cami",
            lastName: String = "Cavalcante",
            cpf: String = "28475934625",
            email: String = "camila@email.com",
            income: BigDecimal = BigDecimal.valueOf(1000.0),
            password: String = "1234",
            zipCode: String = "000000",
            street: String = "Rua da Cami, 123",
    ) = CustomerDto(
            firstName = firstName,
            lastName = lastName,
            cpf = cpf,
            email = email,
            income = income,
            password = password,
            zipCode = zipCode,
            street = street
    )

    fun builderCustomerUpdateDto(
            firstName: String = "CamiUpdate",
            lastName: String = "CavalcanteUpdate",
            income: BigDecimal = BigDecimal.valueOf(5000.0),
            zipCode: String = "45656",
            street: String = "Rua Updated"
    ): CustomerUpdateDto = CustomerUpdateDto(
            firstName = firstName,
            lastName = lastName,
            income = income,
            zipCode = zipCode,
            street = street
    )

    fun buildCustomer(
            id: Long? = null,
            firstName: String = "Cami",
            lastName: String = "Cavalcante",
            cpf: String = "28475934625",
            email: String = "camila@gmail.com",
            password: String = "12345",
            zipCode: String = "12345",
            street: String = "Rua da Cami",
            income: BigDecimal = BigDecimal.valueOf(1000.0)
    ) = Customer(
            id = id,
            firstName = firstName,
            lastName = lastName,
            cpf = cpf,
            email = email,
            password = password,
            address = Address(
                    zipCode = zipCode,
                    street = street,
            ),
            income = income,
    )

    fun buildCredit(customer: Customer = buildCustomer(1L),
                    id: Long? = null,
                    creditCode: UUID = UUID.randomUUID()) = Credit(
            id = id,
            status = Status.IN_PROGRESS,
            creditCode = creditCode,
            creditValue = BigDecimal.ONE,
            dayFirstInstallment = LocalDate.now(),
            numberOfInstallments = 1,
            customer = customer
    )

    fun buildCreditDto(creditValue: BigDecimal = BigDecimal.ONE,
                       dayFirstOfInstallment: LocalDate = LocalDate.now().plusMonths(1),
                       numberOfInstallments: Int = 1,
                       customerId: Long = 1L) = CreditDto(
            creditValue = creditValue,
            dayFirstOfInstallment = dayFirstOfInstallment,
            numberOfInstallments = numberOfInstallments,
            customerId = customerId
    )
}