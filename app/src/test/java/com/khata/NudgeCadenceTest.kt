package com.khata

import com.khata.data.db.TransactionEntity
import com.khata.notifications.NudgeCadence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NudgeCadenceTest {
    private fun purchase(date: String, category: String = "groceries") = TransactionEntity(
        id = date + category,
        rawText = "Milk",
        normalizedMerchant = "milk",
        amountPaise = 6000,
        direction = "DEBIT",
        date = date,
        type = "EXPENSE",
        categoryId = category,
        confidence = 1f,
        source = "USER"
    )

    @Test fun waitsForTwoDatedPurchasesAndTheEstimatedDueDay() {
        val history = listOf(purchase("2026-10-20"), purchase("2026-10-22"))
        assertTrue(NudgeCadence.due(history.take(1), LocalDate.parse("2026-10-30")).isEmpty())
        assertTrue(NudgeCadence.due(history, LocalDate.parse("2026-10-23")).isEmpty())
        val due = NudgeCadence.due(history, LocalDate.parse("2026-10-24"))
        assertEquals(1, due.size)
        assertEquals(2L, due.single().intervalDays)
        assertEquals(LocalDate.parse("2026-10-24"), due.single().dueDate)
    }

    @Test fun ignoresNonTrackedCategories() {
        val history = listOf(purchase("2026-10-20", "dining"), purchase("2026-10-22", "dining"))
        assertTrue(NudgeCadence.due(history, LocalDate.parse("2026-10-24")).isEmpty())
    }
}
