package com.mercadolivro.repository

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
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

    @Test
    fun `should return naming containing with prefix`() {
        val matheus = createCustomer(name = "matheus")
        val maria = createCustomer(name = "maria")
        val alex = createCustomer(name = "alex")
        val expectedCustomers = listOf(matheus, maria)

        val customers = customerRepository.findByNameContaining("ma")

        Assertions.assertEquals(expectedCustomers, customers)
    }


    private fun createCustomer(
        name: String = "Customer name",
        email: String = "${UUID.randomUUID()}@email.com"
    ) = customerRepository.save(CustomerModel(
            id = null,
            name = name,
            email = email,
            status = CustomerStatus.ATIVO,
            password = UUID.randomUUID().toString(),
            roles = emptySet()
    ))
}