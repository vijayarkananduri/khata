package com.khata

import com.khata.domain.stats.budgetProgressFraction
import com.khata.widget.WidgetPeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BudgetProgressTest {
    @Test fun missingOrInvalidBudgetProducesEmptyRing() {
        assertEquals(0f, budgetProgressFraction(500L, 0L), 0f)
        assertEquals(0f, budgetProgressFraction(500L, -1L), 0f)
        assertEquals(0f, budgetProgressFraction(-1L, 500L), 0f)
    }

    @Test fun progressIsBoundedAndHandlesLargeValues() {
        assertEquals(.5f, budgetProgressFraction(50L, 100L), .0001f)
        assertEquals(1f, budgetProgressFraction(101L, 100L), 0f)
        assertEquals(1f, budgetProgressFraction(Long.MAX_VALUE, Long.MAX_VALUE - 1), 0f)
    }

    @Test fun widgetPeriodsUseExpectedBoundaries() {
        val today = LocalDate.of(2026, 10, 25)
        assertEquals(today to today, WidgetPeriod.DAY.dateRange(today))
        assertEquals(today.minusDays(6) to today, WidgetPeriod.WEEK.dateRange(today))
        assertEquals(LocalDate.of(2026, 10, 1) to today, WidgetPeriod.MONTH.dateRange(today))
        assertEquals(LocalDate.of(1, 1, 1) to today, WidgetPeriod.ALL.dateRange(today))
    }
}
