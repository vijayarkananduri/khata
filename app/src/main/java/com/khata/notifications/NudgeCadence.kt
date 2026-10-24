package com.khata.notifications

import com.khata.data.db.TransactionEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class RestockNudge(val key: String, val item: String, val category: String, val intervalDays: Long, val dueDate: LocalDate)

object NudgeCadence {
    private val trackedCategories = setOf("groceries", "fuel", "health", "pet")
    fun due(transactions: List<TransactionEntity>, today: LocalDate): List<RestockNudge> {
        return transactions.asSequence()
            .filter { it.type == "EXPENSE" && it.direction == "DEBIT" && it.categoryId in trackedCategories }
            .groupBy { (it.normalizedMerchant.ifBlank { it.rawText }.trim().lowercase() to it.categoryId) }
            .mapNotNull { (groupKey, rows) ->
                val dates = rows.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.distinct().sorted()
                if (dates.size < 2) return@mapNotNull null
                val gaps = dates.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b) }.filter { it in 1L..90L }.takeLast(4)
                if (gaps.isEmpty()) return@mapNotNull null
                val interval = (gaps.average().toLong()).coerceAtLeast(1L)
                val last = dates.last()
                val due = last.plusDays(interval)
                if (today < due) return@mapNotNull null
                val latest = rows.maxByOrNull { it.date } ?: return@mapNotNull null
                RestockNudge("${groupKey.first}:${groupKey.second}", latest.rawText.ifBlank { groupKey.first }, groupKey.second, interval, due)
            }.toList()
    }
}
