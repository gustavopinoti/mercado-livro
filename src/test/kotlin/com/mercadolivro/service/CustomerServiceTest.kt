package com.mercadolivro.service

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.enums.Role
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*
import kotlin.math.exp

@ExtendWith(SpringExtension::class)
class CustomerServiceTest {

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var bCryptPasswordEncoder: BCryptPasswordEncoder

    @MockK
    private lateinit var bookService: BookService

    @InjectMockKs
    private lateinit var customerService: CustomerService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `should return all customers`() {
        val customer1 = buildCustomer()
        val customer2 = buildCustomer()
        val customers = listOf(customer1, customer2)

        every { customerRepository.findAll() } returns customers

        customerService.getAll(null)

        verify(exactly = 1) {
            customerRepository.findAll()
        }

        verify(exactly = 0) {
            customerRepository.findByNameContaining(any())
        }
    }

    @Test
    fun `should return all customers filtring by name`() {
        val customer1 = buildCustomer()
        val customer2 = buildCustomer()
        val customers = listOf(customer1, customer2)
        val namePrefix = "customer"

        every { customerRepository.findByNameContaining(namePrefix) } returns customers

        customerService.getAll(namePrefix)

        verify(exactly = 0) {
            customerRepository.findAll()
        }

        verify(exactly = 1) {
            customerRepository.findByNameContaining(namePrefix)
        }
    }

    @Test
    fun `should encrpt password when create user`() {
        val initialPassword = "some password"
        val customer = buildCustomer(password = initialPassword)
        val fakeEncodePassword = UUID.randomUUID().toString()
        val expectedCustomer = customer.copy(password = fakeEncodePassword)

        every { customerRepository.save(expectedCustomer) } returns expectedCustomer
        every { bCryptPasswordEncoder.encode(initialPassword) } returns fakeEncodePassword

        customerService.create(customer)

        verify(exactly = 1) {
            customerRepository.save(expectedCustomer)
        }

        verify(exactly = 1) {
            bCryptPasswordEncoder.encode(initialPassword)
        }
    }

    @Test
    fun `should return find customer by id`() {
        val id = Random().nextInt()
        val customer = buildCustomer(id = id)

        every { customerRepository.findById(id) } returns Optional.of(customer)

        customerService.findById(id)

        verify(exactly = 1) {
            customerRepository.findById(id)
        }
    }

    @Test
    fun `should throw erro when customer not found by id`() {
        val id = Random().nextInt()
        val expectedErrorMessage = Errors.ML201.message.format(id)
        val expectedErrorCode = Errors.ML201.code

        every { customerRepository.findById(id) } returns Optional.empty()

        val error = Assertions.assertThrows(NotFoundException::class.java) {
            customerService.findById(id)
        }

        Assertions.assertEquals(expectedErrorMessage, error.message)
        Assertions.assertEquals(expectedErrorCode, error.errorCode)

        verify(exactly = 1) {
            customerRepository.findById(id)
        }
    }


    private fun buildCustomer(
            id: Int? = null,
            name: String = "customer name",
            email: String = "${UUID.randomUUID()}@email.com",
            password: String = "password"
    ) = CustomerModel(
            id = id,
            name = name,
            email = email,
            status = CustomerStatus.ATIVO,
            password = password,
            roles = setOf(Role.CUSTOMER)
    )

}