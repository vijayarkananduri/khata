package com.khata.domain.stats

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SpendPeriodRangesTest {
    @Test fun dailyPeriodsCompareAgainstYesterday() {
        val today = LocalDate.parse("2026-10-24")
        assertEquals(DateRange(today, today), SpendPeriodRanges.current(SpendPeriod.DAY, today))
        assertEquals(DateRange(today.minusDays(1), today.minusDays(1)), SpendPeriodRanges.previous(SpendPeriod.DAY, today))
    }

    @Test fun weeklyPeriodsAreAdjacentSevenDayWindows() {
        val today = LocalDate.parse("2026-10-24")
        assertEquals(DateRange(today.minusDays(6), today), SpendPeriodRanges.current(SpendPeriod.WEEK, today))
        assertEquals(DateRange(today.minusDays(13), today.minusDays(7)), SpendPeriodRanges.previous(SpendPeriod.WEEK, today))
    }

    @Test fun monthlyComparisonCoversFullPreviousMonthIncludingLeapDay() {
        val today = LocalDate.parse("2024-03-12")
        assertEquals(DateRange(LocalDate.parse("2024-03-01"), today), SpendPeriodRanges.current(SpendPeriod.MONTH, today))
        assertEquals(DateRange(LocalDate.parse("2024-02-01"), LocalDate.parse("2024-02-29")), SpendPeriodRanges.previous(SpendPeriod.MONTH, today))
    }
}
