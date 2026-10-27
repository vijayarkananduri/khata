package com.khata

import com.khata.domain.quotes.AmbientQuoteCandidate
import com.khata.domain.quotes.AmbientQuotePicker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class AmbientQuotePickerTest {
    @Test fun paydayAndMonthEndTakePriorityOverTimeOfDay() {
        assertEquals("payday", AmbientQuotePicker.currentLane(LocalDateTime.of(2026, 10, 1, 22, 0)))
        assertEquals("month_end", AmbientQuotePicker.currentLane(LocalDateTime.of(2026, 10, 28, 9, 0)))
    }

    @Test fun ordinaryDaysUseTheRequestedTimeLanes() {
        assertEquals("night", AmbientQuotePicker.currentLane(LocalDateTime.of(2026, 10, 12, 4, 59)))
        assertEquals("morning", AmbientQuotePicker.currentLane(LocalDateTime.of(2026, 10, 12, 5, 0)))
        assertEquals("afternoon", AmbientQuotePicker.currentLane(LocalDateTime.of(2026, 10, 12, 12, 0)))
        assertEquals("evening", AmbientQuotePicker.currentLane(LocalDateTime.of(2026, 10, 12, 17, 0)))
        assertEquals("night", AmbientQuotePicker.currentLane(LocalDateTime.of(2026, 10, 12, 21, 0)))
    }

    @Test fun selectionAvoidsTheLastIdAndToneWhenAlternativesExist() {
        val choices = listOf(
            AmbientQuoteCandidate("one", "wise", "morning", "A", "A"),
            AmbientQuoteCandidate("two", "witty", "morning", "B", "B"),
            AmbientQuoteCandidate("three", "quiet", "night", "C", "C")
        )
        val selected = AmbientQuotePicker.choose(choices, "morning", lastTone = "wise", lastId = "one")!!
        assertNotEquals("one", selected.id)
        assertNotEquals("wise", selected.tone)
    }

    @Test fun missingLaneFallsBackToTheAvailableCatalog() {
        val choices = listOf(AmbientQuoteCandidate("night", "quiet", "night", "A", "A"))
        val selected = AmbientQuotePicker.choose(choices, "afternoon", null, null)
        assertTrue(selected === choices.single())
    }
}
