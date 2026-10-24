package com.khata.domain.stats

import java.time.LocalDate

enum class SpendPeriod(val key: String, val label: String) {
    DAY("DAY", "Today"),
    WEEK("WEEK", "Last 7 days"),
    MONTH("MONTH", "This month");

    companion object {
        fun fromKey(key: String): SpendPeriod = entries.firstOrNull { it.key == key } ?: MONTH
    }
}

data class DateRange(val start: LocalDate, val endInclusive: LocalDate) {
    fun contains(isoDate: String): Boolean = isoDate >= start.toString() && isoDate <= endInclusive.toString()
}

object SpendPeriodRanges {
    fun current(period: SpendPeriod, today: LocalDate): DateRange = when (period) {
        SpendPeriod.DAY -> DateRange(today, today)
        SpendPeriod.WEEK -> DateRange(today.minusDays(6), today)
        SpendPeriod.MONTH -> DateRange(today.withDayOfMonth(1), today)
    }

    fun previous(period: SpendPeriod, today: LocalDate): DateRange = when (period) {
        SpendPeriod.DAY -> DateRange(today.minusDays(1), today.minusDays(1))
        SpendPeriod.WEEK -> DateRange(today.minusDays(13), today.minusDays(7))
        SpendPeriod.MONTH -> {
            val previousMonth = today.minusMonths(1)
            DateRange(previousMonth.withDayOfMonth(1), previousMonth.withDayOfMonth(previousMonth.lengthOfMonth()))
        }
    }
}
