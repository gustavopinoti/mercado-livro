package com.mercadolivro.service

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.enums.Role
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.helpers.buildCustomer
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ActiveProfiles("test")
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

        val returnedCustomers = customerService.getAll(null)

        assertEquals(customers, returnedCustomers)
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

        val returnedCustomers = customerService.getAll(namePrefix)

        assertEquals(customers, returnedCustomers)
        verify(exactly = 0) {
            customerRepository.findAll()
        }
        verify(exactly = 1) {
            customerRepository.findByNameContaining(namePrefix)
        }
    }

    @Test
    fun `should encode password when create user`() {
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

        val returnedCustomer = customerService.findById(id)

        assertEquals(customer, returnedCustomer)
        verify(exactly = 1) {
            customerRepository.findById(id)
        }
    }

    @Test
    fun `should throw error when customer not found by id`() {
        val id = Random().nextInt()
        val expectedErrorMessage = Errors.ML201.message.format(id)
        val expectedErrorCode = Errors.ML201.code

        every { customerRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<NotFoundException> {
            customerService.findById(id)
        }

        assertEquals(expectedErrorMessage, error.message)
        assertEquals(expectedErrorCode, error.errorCode)

        verify(exactly = 1) {
            customerRepository.findById(id)
        }
    }

    @Test
    fun `should update user`() {
        val id = Random().nextInt()
        val customer = buildCustomer(id = id)
        val newCustomer = customer.copy(
                name = "New name",
                email = "New email"
        )

        every { customerRepository.existsById(id) } returns true
        every { customerRepository.save(newCustomer) } returns newCustomer

        customerService.update(newCustomer)

        verify(exactly = 1) {
            customerRepository.save(newCustomer)
        }
    }

    @Test
    fun `should throws not found exception when customer not found`() {
        val id = Random().nextInt()
        val fakeCustomer = buildCustomer(id = id)
        val expectedErrorMessage = Errors.ML201.message.format(id)
        val expectedErrorCode = Errors.ML201.code

        every { customerRepository.existsById(id) } returns false

        val error = assertThrows<NotFoundException> {
            customerService.update(fakeCustomer)
        }

        assertEquals(expectedErrorMessage, error.message)
        assertEquals(expectedErrorCode, error.errorCode)
    }

    @Test
    fun `should delete user`() {
        val id = Random().nextInt()
        val customer = buildCustomer(id = id)
        val expectedCustomer = customer.copy(status = CustomerStatus.INATIVO)

        every { customerRepository.findById(id) } returns Optional.of(customer)
        every { customerRepository.save(expectedCustomer) } returns expectedCustomer
        every { bookService.deleteByCustomer(customer) } just runs

        customerService.delete(id)

        verify(exactly = 1) {
            bookService.deleteByCustomer(customer)
        }
        verify(exactly = 1) {
            customerRepository.save(customer)
        }
    }

    @Test
    fun `should return true if email is available`() {
        val email = "${UUID.randomUUID()}@gmail.com"

        every { customerRepository.existsByEmail(email) } returns false

        val result = customerService.emailAvailable(email)

        assertTrue(result)
    }

    @Test
    fun `should return true if email is not available`() {
        val email = "${UUID.randomUUID()}@gmail.com"

        every { customerRepository.existsByEmail(email) } returns true

        val result = customerService.emailAvailable(email)

        assertFalse(result)
    }
}