package com.mercadolivro.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mercadolivro.controller.request.PostCustomerRequest
import com.mercadolivro.controller.request.PutCustomerRequest
import com.mercadolivro.enums.Errors
import com.mercadolivro.enums.Role
import com.mercadolivro.helpers.buildCustomer
import com.mercadolivro.repository.CustomerRepository
import com.mercadolivro.security.UserCustomDetails
import com.mercadolivro.service.UserDetailsCustomService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@WithMockUser //mocka um token valido para as requests, se colocar na classe para todas
class CustomerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mapper: ObjectMapper


    @BeforeEach
    fun setUp() {
        customerRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        customerRepository.deleteAll()
    }

    @Test
    fun `should get all users`() {
        val customerOne = customerRepository.save(buildCustomer())
        val customerTwo = customerRepository.save(buildCustomer())
        mockMvc.perform(get("/customer"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isNotEmpty)
    }

    @Test
    fun `should get all user filtring by name`() {
        val customerOne = customerRepository.save(buildCustomer(name = "Ana"))
        val customerTwo = customerRepository.save(buildCustomer())
        mockMvc.perform(get("/customer?name=ana"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should get user by id when the user is admin`() {
        val customer = customerRepository.save(buildCustomer())
        mockMvc.perform(get("/customer/${customer.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(customer.id))
                .andExpect(jsonPath("$.name").value(customer.name))
                .andExpect(jsonPath("$.email").value(customer.email))
                .andExpect(jsonPath("$.status").value(customer.status.toString()))
    }

    @Test
    fun `should get user by id when the user has the same id`() {
        val customer = customerRepository.save(buildCustomer(id = 1, email = "customer@customer.com"))
        mockMvc.perform(get("/customer/${customer.id}").with(user(UserCustomDetails(customer))))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(customer.id))
                .andExpect(jsonPath("$.name").value(customer.name))
                .andExpect(jsonPath("$.email").value(customer.email))
                .andExpect(jsonPath("$.status").value(customer.status.toString()))
    }

    @Test
    fun `should return forbidden when user no has the same id`() {
        val customer = customerRepository.save(buildCustomer(id = 1, email = "customer@customer.com"))
        mockMvc.perform(get("/customer/2").with(user(UserCustomDetails(customer))))
                .andExpect(status().isForbidden)
    }

    @Test
    fun `should create customer`() {
        val requestBody = PostCustomerRequest("NewCustomer", "newcustomer@gmail.com", "123456")
        mockMvc.perform(post("/customer").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated)
    }

    @Test
    fun `should throw validation error if required param was not pass when create`() {
        val requestBody = PostCustomerRequest("", "newcustomer@gmail.com", "123456")
        mockMvc.perform(post("/customer").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `should update customer`() {
        val customer = customerRepository.save(buildCustomer())
        val requestBody = PutCustomerRequest("NewName", "newemail@gmail.com")
        mockMvc.perform(put("/customer/${customer.id}").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(requestBody)))
                .andExpect(status().isNoContent)
    }

    @Test
    fun `should throw validation error if required param was not pass when update`() {
        val customer = customerRepository.save(buildCustomer())
        val requestBody = PutCustomerRequest("", "newemail@gmail.com")
        mockMvc.perform(put("/customer/${customer.id}").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `should delete customer`() {
        val customer = customerRepository.save(buildCustomer())
        mockMvc.perform(delete("/customer/${customer.id}"))
                .andExpect(status().isNoContent)
    }

    @Test
    fun `should throw not found if customer not found when deleting`() {
        val customer = customerRepository.save(buildCustomer())
        val fakeId = Int.MAX_VALUE
        mockMvc.perform(delete("/customer/$fakeId"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Customer [$fakeId] not exists"))
                .andExpect(jsonPath("$.internalCode").value(Errors.ML201.code))
    }
}