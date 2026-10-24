package com.khata.data.repo

import com.khata.data.db.KhataDao
import com.khata.data.db.MerchantRuleEntity
import com.khata.data.db.TransactionEntity
import com.khata.domain.categorizer.Categorizer
import com.khata.domain.categorizer.KhataCategories
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KhataRepository @Inject constructor(private val dao: KhataDao) {
    val transactions: Flow<List<TransactionEntity>> = dao.observeTransactions()
    val reviewQueue: Flow<List<TransactionEntity>> = dao.observeReviewQueue()
    val categories = dao.observeCategories()

    suspend fun seedCategories() {
        KhataCategories.seedCategories().forEach { dao.insertCategory(it) }
    }

    suspend fun addTransaction(merchant: String, amountPaise: Long, direction: String, date: LocalDate, note: String, chosenCategory: String? = null) {
        val normalized = Categorizer.normalize(merchant)
        val old = dao.transactionsBetween(date.toString(), date.toString())
        if (old.any { it.normalizedMerchant == normalized && it.amountPaise == amountPaise && it.direction == direction && it.accountId == "manual" }) return
        val result = Categorizer.classify(merchant, direction, dao.getRules())
        val category = chosenCategory ?: result.categoryId
        dao.insertTransaction(
            TransactionEntity(
                id = UUID.randomUUID().toString(), rawText = merchant, normalizedMerchant = normalized,
                amountPaise = amountPaise, direction = direction, date = date.toString(), type = result.type,
                categoryId = category, confidence = if (chosenCategory != null) 1f else result.confidence,
                source = if (chosenCategory != null) "USER" else result.source,
                isReviewed = category != "uncategorized", note = note
            )
        )
        if (chosenCategory != null) ruleFor(normalized, chosenCategory)
    }

    suspend fun correct(transaction: TransactionEntity, categoryId: String, transfer: Boolean = false) {
        val type = if (transfer) "TRANSFER" else transaction.type
        val category = if (transfer) "transfers" else categoryId
        dao.correctTransaction(transaction.id, category, type)
        if (!transfer && categoryId != "uncategorized") {
            val merchant = transaction.normalizedMerchant
            if (merchant.isNotBlank()) {
                dao.insertRule(MerchantRuleEntity(UUID.randomUUID().toString(), merchant, "CONTAINS", category, priority = 100, isUserCreated = true))
                dao.retroactivelyCategorize(merchant, category)
            }
        }
    }

    suspend fun ignore(id: String) = dao.ignoreTransaction(id)
    suspend fun delete(id: String) = dao.deleteTransaction(id)
    suspend fun clearAll() = dao.clearTransactions()
    suspend fun ruleFor(pattern: String, categoryId: String) {
        dao.insertRule(MerchantRuleEntity(UUID.randomUUID().toString(), pattern, "CONTAINS", categoryId, priority = 100, isUserCreated = true))
    }
}
