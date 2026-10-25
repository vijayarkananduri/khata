package com.khata

import com.khata.domain.quotes.QuoteRotation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteRotationTest {
    @Test
    fun firstQuoteIsEligibleImmediately() {
        assertTrue(QuoteRotation.shouldRotate(lastShownAtMillis = 0L, nowMillis = 1L))
    }

    @Test
    fun quoteDoesNotRotateBeforeTwelveHoursButDoesAtBoundary() {
        val last = 10_000L
        assertFalse(QuoteRotation.shouldRotate(last, last + QuoteRotation.DEFAULT_INTERVAL_MS - 1L))
        assertTrue(QuoteRotation.shouldRotate(last, last + QuoteRotation.DEFAULT_INTERVAL_MS))
    }

    @Test
    fun clockMovingBackAllowsRefreshInsteadOfPinningTheOldQuote() {
        assertTrue(QuoteRotation.shouldRotate(lastShownAtMillis = 20_000L, nowMillis = 19_000L))
    }
}
