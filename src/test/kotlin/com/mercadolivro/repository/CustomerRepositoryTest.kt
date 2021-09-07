package com.mercadolivro.repository

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.helpers.buildCustomer
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@SpringBootTest
@ExtendWith(SpringExtension::class)
class CustomerRepositoryTest {

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @BeforeEach
    fun setUp() = customerRepository.deleteAll()

    @Nested
    inner class FindByNameContaining {
        @Test
        fun `should return naming containing with prefix`() {
            val matheus = customerRepository.save(buildCustomer(name = "matheus"))
            val maria = customerRepository.save(buildCustomer(name = "maria"))
            val alex = customerRepository.save(buildCustomer(name = "alex"))
            val expectedCustomers = listOf(matheus, maria)

            val customers = customerRepository.findByNameContaining("ma")

            assertEquals(expectedCustomers, customers)
        }
    }

    @Nested
    inner class ExistsByEmail() {
        @Test
        fun `should return true when has email`() {
            val email = "customeremail@gmail.com"
            val customer = customerRepository.save(buildCustomer(email = email))

            val result = customerRepository.existsByEmail(email)

            assertTrue(result)
        }

        @Test
        fun `should return null when not has email`() {
            val email = "nonexistentemail@gmail.com"

            val result = customerRepository.existsByEmail(email)

            assertFalse(result)
        }
    }

    @Nested
    inner class FindByEmail() {
        @Test
        fun `should return customer when has email`() {
            val email = "customeremail@gmail.com"
            val customer = customerRepository.save(buildCustomer(email = email))

            val result = customerRepository.findByEmail(email)

            assertNotNull(result)
            assertEquals(customer, result)
        }

        @Test
        fun `should return null when not has email`() {
            val email = "nonexistentemail@gmail.com"

            val result = customerRepository.findByEmail(email)

            assertNull(result)
        }
    }

}