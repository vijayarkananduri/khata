package com.khata

import com.khata.domain.categorizer.Categorizer
import org.junit.Assert.assertEquals
import org.junit.Test

class CategorizerTest {
    @Test fun knownDiningMerchantUsesKeywordRule() {
        val result = Categorizer.classify("Swiggy", "DEBIT")
        assertEquals("dining", result.categoryId)
        assertEquals("EXPENSE", result.type)
        assertEquals("KEYWORD", result.source)
    }

    @Test fun unknownDebitNeedsReviewInsteadOfGuessing() {
        val result = Categorizer.classify("XYZ / 12345678 @okaxis", "DEBIT")
        assertEquals("uncategorized", result.categoryId)
        assertEquals("XYZ /", Categorizer.normalize("XYZ / 12345678 @okaxis").uppercase())
    }

    @Test fun creditCashbackIsClassifiedAsIncome() {
        val result = Categorizer.classify("cashback credit from store", "CREDIT")
        assertEquals("INCOME", result.type)
        assertEquals("income", result.categoryId)
    }

    @Test fun investmentMerchantIsNotTreatedAsExpense() {
        val result = Categorizer.classify("Zerodha SIP", "DEBIT")
        assertEquals("INVESTMENT", result.type)
        assertEquals("investments", result.categoryId)
    }

    @Test fun feesAreClassifiedBeforeExpenseKeywords() {
        val result = Categorizer.classify("annual fee charged", "DEBIT")
        assertEquals("FEE", result.type)
        assertEquals("fees", result.categoryId)
    }
}
