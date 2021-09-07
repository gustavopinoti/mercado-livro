package com.mercadolivro.controller

import com.mercadolivro.enums.Role
import com.mercadolivro.helpers.buildCustomer
import com.mercadolivro.repository.CustomerRepository
import com.mercadolivro.security.UserCustomDetails
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@WithMockUser
class CustomerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @BeforeEach
    fun setUp() { customerRepository.save(buildCustomer(id = 1, email = "customer@customer.com")) }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should get user by id`() {
        val customer = customerRepository.save(buildCustomer())
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/${customer.id}"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(customer.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(customer.name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(customer.email))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(customer.status.toString()))
    }

    @Test
    @WithUserDetails(value="1", userDetailsServiceBeanName="userDetailsCustomService")
    fun `should get user by id 123`() {
        val customer = customerRepository.findByEmail("customer@customer.com")!!
        mockMvc.perform(MockMvcRequestBuilders.get("/customer/${customer.id}"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(customer.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(customer.name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(customer.email))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(customer.status.toString()))
    }

    @Test
    fun `should get all users`() {
        val customerOne = customerRepository.save(buildCustomer())
        val customerTwo = customerRepository.save(buildCustomer())
        mockMvc.perform(MockMvcRequestBuilders.get("/customer"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty)
    }

    @Test
    fun `should get all user filtring by name`() {
        val customerOne = customerRepository.save(buildCustomer(name = "Ana"))
        val customerTwo = customerRepository.save(buildCustomer())
        mockMvc.perform(MockMvcRequestBuilders.get("/customer?name=ana"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.orders.length()").value(1))
    }

}