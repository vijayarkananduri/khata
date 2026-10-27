package com.khata

import com.khata.data.backup.CsvTransactionCodec
import com.khata.data.db.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CsvTransactionCodecTest {
    @Test fun currentExportRoundTripsQuotesCommasPaiseAndMetadata() {
        val original = TransactionEntity(
            id = "txn-1", rawText = "Cafe, \"Blue\"", normalizedMerchant = "cafe blue",
            amountPaise = 12345L, direction = "DEBIT", date = "2026-10-25", accountId = "manual",
            type = "EXPENSE", categoryId = "dining", confidence = .85f, source = "USER",
            isReviewed = true, note = "Lunch, with a friend\npaid by card"
        )
        val restored = CsvTransactionCodec.decode(
            CsvTransactionCodec.encode(listOf(original)), "manual", setOf("manual")
        ).single()
        assertEquals(original.id, restored.id)
        assertEquals(original.rawText, restored.rawText)
        assertEquals(original.amountPaise, restored.amountPaise)
        assertEquals(original.direction, restored.direction)
        assertEquals(original.date, restored.date)
        assertEquals(original.accountId, restored.accountId)
        assertEquals(original.type, restored.type)
        assertEquals(original.categoryId, restored.categoryId)
        assertEquals(original.note, restored.note)
        assertEquals(original.source, restored.source)
    }

    @Test fun legacyKhataCsvWithoutIdsImportsWithStableIds() {
        val csv = "date,merchant,amount_rupees,direction,type,category,note\n2026-10-24,Tea shop,40.50,DEBIT,EXPENSE,dining,Tea"
        val first = CsvTransactionCodec.decode(csv, "manual").single()
        val second = CsvTransactionCodec.decode(csv, "manual").single()
        assertEquals(first.id, second.id)
        assertEquals(4050L, first.amountPaise)
        assertEquals("IMPORT", first.source)
    }

    @Test fun invalidRowsRejectTheWholeFile() {
        val csv = "date,merchant,amount_rupees,direction\nnot-a-date,Cafe,12.00,DEBIT"
        assertThrows(IllegalArgumentException::class.java) {
            CsvTransactionCodec.decode(csv, "manual")
        }
    }

    @Test fun refundRowsMustBeCredits() {
        val csv = "date,merchant,amount_rupees,direction,type,category\n2026-10-24,Store,12.00,DEBIT,REFUND,shopping"
        assertThrows(IllegalArgumentException::class.java) {
            CsvTransactionCodec.decode(csv, "manual")
        }
    }
}
