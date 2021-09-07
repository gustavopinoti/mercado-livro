package com.mercadolivro.events.listener

import com.mercadolivro.events.PurchaseEvent
import com.mercadolivro.helpers.buildPurchase
import com.mercadolivro.repository.PurchaseRepository
import com.mercadolivro.service.PurchaseService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
class GenerateNfeListenerTest {

    @MockK
    private lateinit var purchaseRepository: PurchaseService

    @InjectMockKs
    private lateinit var generateNfeListener: GenerateNfeListener

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `should generate nfe`() {
        val someNfe = UUID.randomUUID()
        mockkStatic(UUID::class)
        val purchase = buildPurchase(id = Random().nextInt())
        val expectedPurchase = purchase.copy(nfe = someNfe.toString())
        val event = PurchaseEvent(this, purchase)

        every { purchaseRepository.update(expectedPurchase) } just runs
        every { UUID.randomUUID() } returns someNfe

        generateNfeListener.listen(event)

        verify(exactly = 1) {
            purchaseRepository.update(expectedPurchase)
        }

        unmockkStatic(UUID::class)
    }

    fun buildEvent() = PurchaseEvent(
            this,
            buildPurchase()
    )

}
