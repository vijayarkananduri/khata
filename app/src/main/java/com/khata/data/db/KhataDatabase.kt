package com.khata.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val rawText: String,
    val normalizedMerchant: String,
    val amountPaise: Long,
    val direction: String,
    val date: String,
    val accountId: String = "manual",
    val type: String,
    val categoryId: String,
    val confidence: Float,
    val source: String,
    val isReviewed: Boolean = true,
    val note: String = ""
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val colorKey: String,
    val parentId: String? = null,
    val isSystem: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(tableName = "merchant_rules", indices = [Index(value = ["priority"])])
data class MerchantRuleEntity(
    @PrimaryKey val id: String,
    val pattern: String,
    val matchType: String,
    val categoryId: String,
    val typeOverride: String? = null,
    val priority: Int = 10,
    val isUserCreated: Boolean = false
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val institution: String,
    val accountType: String,
    val openingBalancePaise: Long = 0L,
    val isActive: Boolean = true,
    val isLinkedViaAa: Boolean = false,
    val createdAt: String
)

@Dao
interface KhataDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC LIMIT 500")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = 'uncategorized' AND isReviewed = 0 ORDER BY date DESC")
    fun observeReviewQueue(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM merchant_rules ORDER BY priority DESC")
    suspend fun getRules(): List<MerchantRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: MerchantRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY createdAt, name")
    fun observeAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun countAccounts(): Int

    @Query("UPDATE transactions SET categoryId = :category, source = 'USER', confidence = 1.0, isReviewed = 1, type = :type WHERE id = :id")
    suspend fun correctTransaction(id: String, category: String, type: String = "EXPENSE")

    @Query("UPDATE transactions SET categoryId = :category, source = 'LEARNED', confidence = 1.0, isReviewed = 1 WHERE normalizedMerchant = :merchant AND categoryId = 'uncategorized'")
    suspend fun retroactivelyCategorize(merchant: String, category: String)

    @Query("UPDATE transactions SET isReviewed = 1 WHERE id = :id")
    suspend fun ignoreTransaction(id: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("SELECT * FROM categories ORDER BY sortOrder")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'EXPENSE' AND direction = 'DEBIT' AND categoryId IN ('groceries', 'fuel', 'health', 'pet') ORDER BY date DESC LIMIT 500")
    suspend fun nudgePurchases(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    suspend fun transactionsBetween(from: String, to: String): List<TransactionEntity>
}

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, MerchantRuleEntity::class, AccountEntity::class],
    version = 2,
    exportSchema = true
)
abstract class KhataDatabase : RoomDatabase() {
    abstract fun dao(): KhataDao
}
