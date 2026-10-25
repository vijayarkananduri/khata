package com.khata.widget

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WidgetPeriodTest {
    private val today = LocalDate.parse("2026-10-25")

    @Test fun periodsUseExpectedDateRangesIncludingAllTime() {
        assertEquals(today to today, WidgetPeriod.DAY.dateRange(today))
        assertEquals(today.minusDays(6) to today, WidgetPeriod.WEEK.dateRange(today))
        assertEquals(LocalDate.parse("2026-10-01") to today, WidgetPeriod.MONTH.dateRange(today))
        assertEquals(LocalDate.of(1, 1, 1) to today, WidgetPeriod.ALL.dateRange(today))
    }

    @Test fun unknownStoredPeriodFallsBackToDay() {
        assertEquals(WidgetPeriod.DAY, WidgetPeriod.fromKey(null))
        assertEquals(WidgetPeriod.DAY, WidgetPeriod.fromKey("unsupported"))
        assertEquals(WidgetPeriod.ALL, WidgetPeriod.fromKey("ALL"))
    }

    @Test fun sizeVariantsSelectCompactStandardAndLargeLayouts() {
        assertEquals(WidgetLayoutVariant.COMPACT, widgetLayoutVariantFor(220, 156))
        assertEquals(WidgetLayoutVariant.STANDARD, widgetLayoutVariantFor(300, 180))
        assertEquals(WidgetLayoutVariant.LARGE, widgetLayoutVariantFor(340, 220))
    }
}
