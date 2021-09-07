package com.mercadolivro.service

import com.mercadolivro.events.PurchaseEvent
import com.mercadolivro.helpers.buildPurchase
import com.mercadolivro.repository.PurchaseRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("test")
@SpringBootTest
@ExtendWith(SpringExtension::class)
class PurchaseTest {

    @MockK
    private lateinit var purchaseRepository: PurchaseRepository

    @MockK
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @InjectMockKs
    private lateinit var purchaseService: PurchaseService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    val purchaseEventSlot = slot<PurchaseEvent>()

    @Test
    fun `should update purchase`() {
        val purchase = buildPurchase()

        every { purchaseRepository.save(any()) } returns purchase

       purchaseService.update(purchase)

        verify(exactly = 1) {
            purchaseRepository.save(purchase)
        }

        verify(exactly = 0) {
            applicationEventPublisher.publishEvent(any())
        }
    }

    @Test
    fun `should create purchase`() {
        val purchase = buildPurchase()

        every { purchaseRepository.save(any()) } returns purchase

        every { applicationEventPublisher.publishEvent(any()) } just runs

        purchaseService.create(purchase)

        verify(exactly = 1) {
            purchaseRepository.save(purchase)
        }

        verify(exactly = 1) {
            applicationEventPublisher.publishEvent(capture(purchaseEventSlot))
        }

        Assertions.assertEquals(purchase, purchaseEventSlot.captured.purchaseModel)
    }





}