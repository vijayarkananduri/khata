package com.khata.data.backup

import com.khata.data.db.TransactionEntity
import com.khata.domain.categorizer.Categorizer
import java.math.BigDecimal
import java.math.RoundingMode
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.UUID

/** CSV import/export for transaction rows. Imports are validated completely before insertion. */
object CsvTransactionCodec {
    private const val MAX_CHARACTERS = 10_000_000
    private const val MAX_ROWS = 100_000
    private val header = listOf(
        "id", "date", "merchant", "amount_rupees", "direction", "type", "category",
        "account_id", "note", "confidence", "source", "is_reviewed"
    )
    private val supportedTypes = setOf("EXPENSE", "REFUND", "INCOME", "TRANSFER")

    fun encode(transactions: List<TransactionEntity>): String = buildString {
        appendLine(header.joinToString(",") { escape(it) })
        transactions.forEach { item ->
            val fields = listOf(
                item.id,
                item.date,
                item.rawText,
                BigDecimal.valueOf(item.amountPaise, 2).toPlainString(),
                item.direction,
                item.type,
                item.categoryId,
                item.accountId,
                item.note,
                item.confidence.toString(),
                item.source,
                item.isReviewed.toString()
            )
            appendLine(fields.joinToString(",") { escape(it) })
        }
    }

    /**
     * Supports both the current schema and the original Khata CSV header
     * (date,merchant,amount_rupees,direction,type,category,note).
     * Rows with invalid required fields reject the whole file; no partial import occurs.
     */
    fun decode(
        csv: String,
        defaultAccountId: String,
        validAccountIds: Set<String> = setOf(defaultAccountId)
    ): List<TransactionEntity> {
        require(csv.length <= MAX_CHARACTERS) { "CSV is too large to import." }
        require(defaultAccountId.isNotBlank()) { "No destination account is available." }
        val rows = parseRows(csv)
        require(rows.isNotEmpty()) { "The CSV file is empty." }
        val columns = rows.first().mapIndexed { index, name ->
            val cleaned = name.trim().removePrefix("\uFEFF").lowercase()
            cleaned to index
        }.toMap()
        listOf("date", "merchant", "amount_rupees", "direction").forEach { required ->
            require(columns.containsKey(required)) { "Missing required CSV column: $required" }
        }
        val dataRows = rows.drop(1).filter { row -> row.any { it.isNotBlank() } }
        require(dataRows.size <= MAX_ROWS) { "The CSV contains more than $MAX_ROWS rows." }
        require(dataRows.isNotEmpty()) { "No transaction rows were found." }

        return dataRows.mapIndexed { rowIndex, row ->
            val lineNumber = rowIndex + 2
            fun value(name: String): String = columns[name]?.let { row.getOrNull(it).orEmpty().trim() }.orEmpty()
            try {
                val date = LocalDate.parse(value("date"))
                val merchant = value("merchant").take(500)
                require(merchant.isNotBlank()) { "Merchant is empty." }
                val amountPaise = BigDecimal(value("amount_rupees"))
                    .movePointRight(2)
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact()
                require(amountPaise > 0L) { "Amount must be greater than zero." }
                val direction = value("direction").uppercase().ifBlank { "DEBIT" }
                require(direction == "DEBIT" || direction == "CREDIT") { "Direction must be DEBIT or CREDIT." }
                val importedType = value("type").uppercase()
                val type = importedType.takeIf { it in supportedTypes }
                    ?: if (direction == "CREDIT") "INCOME" else "EXPENSE"
                require(type != "REFUND" || direction == "CREDIT") { "Refunds must be CREDIT entries." }
                val category = value("category").ifBlank { "uncategorized" }.take(120)
                val importedAccount = value("account_id")
                val accountId = importedAccount.takeIf { it in validAccountIds } ?: defaultAccountId
                val note = value("note").take(2_000)
                val confidence = value("confidence").toFloatOrNull()?.coerceIn(0f, 1f)
                    ?: if (category == "uncategorized") 0f else 1f
                val source = value("source").ifBlank { "IMPORT" }.take(80)
                val reviewed = value("is_reviewed").toBooleanStrictOrNull() ?: (category != "uncategorized")
                val importedId = value("id")
                require(importedId.length <= 128) { "Transaction ID is too long." }
                val id = importedId.ifBlank {
                    val stableMaterial = listOf(
                        lineNumber.toString(), date.toString(), merchant, amountPaise.toString(),
                        direction, type, category, accountId, note
                    ).joinToString("|")
                    UUID.nameUUIDFromBytes(stableMaterial.toByteArray(StandardCharsets.UTF_8)).toString()
                }
                TransactionEntity(
                    id = id,
                    rawText = merchant,
                    normalizedMerchant = Categorizer.normalize(merchant),
                    amountPaise = amountPaise,
                    direction = direction,
                    date = date.toString(),
                    accountId = accountId,
                    type = type,
                    categoryId = category,
                    confidence = confidence,
                    source = source,
                    isReviewed = reviewed,
                    note = note
                )
            } catch (error: Exception) {
                throw IllegalArgumentException("Invalid CSV row $lineNumber: ${error.message ?: "check the date, amount, and direction"}", error)
            }
        }
    }

    private fun parseRows(csv: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var index = 0

        fun finishField() {
            row += field.toString()
            field.setLength(0)
        }

        fun finishRow() {
            finishField()
            rows += row
            row = mutableListOf()
        }

        while (index < csv.length) {
            val character = csv[index]
            when {
                inQuotes && character == '"' && index + 1 < csv.length && csv[index + 1] == '"' -> {
                    field.append('"')
                    index++
                }
                inQuotes && character == '"' -> inQuotes = false
                inQuotes -> field.append(character)
                character == '"' && field.isEmpty() -> inQuotes = true
                character == ',' -> finishField()
                character == '\n' || character == '\r' -> {
                    if (character == '\r' && index + 1 < csv.length && csv[index + 1] == '\n') index++
                    finishRow()
                }
                else -> field.append(character)
            }
            index++
        }
        require(!inQuotes) { "CSV contains an unterminated quoted field." }
        if (field.isNotEmpty() || row.isNotEmpty()) finishRow()
        return rows
    }

    private fun escape(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
}
