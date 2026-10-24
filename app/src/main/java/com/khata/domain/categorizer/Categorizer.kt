package com.khata.domain.categorizer

import com.khata.data.db.MerchantRuleEntity
import java.util.Locale

object KhataCategories {
    val names = linkedMapOf(
        "groceries" to "Groceries", "dining" to "Dining", "transport" to "Transport",
        "utilities" to "Utilities", "health" to "Health", "shopping" to "Shopping",
        "entertainment" to "Entertainment", "subscriptions" to "Subscriptions",
        "fuel" to "Fuel", "education" to "Education", "rent" to "Rent",
        "fees" to "Bank Fees", "transfers" to "Transfers", "income" to "Income",
        "investments" to "Investments", "uncategorized" to "Uncategorized"
    )
    val colors = mapOf(
        "groceries" to "tide", "dining" to "ember", "transport" to "saffron",
        "utilities" to "plum", "health" to "tide", "shopping" to "ember",
        "entertainment" to "saffron", "subscriptions" to "plum", "fuel" to "ember",
        "education" to "tide", "rent" to "ink", "fees" to "brick",
        "transfers" to "muted", "income" to "tide", "investments" to "tide",
        "uncategorized" to "muted"
    )
    val keywords = listOf(
        "groceries" to Regex("grocery|kirana|supermarket|fresh|provision|mart"),
        "dining" to Regex("restaurant|cafe|food|dhaba|bakery|biryani|pizza|swiggy|zomato"),
        "fuel" to Regex("petrol|fuel|hp |indianoil|bharat petroleum|shell"),
        "health" to Regex("pharmacy|medical|apollo|1mg|pharmeasy|netmeds"),
        "utilities" to Regex("electricity|water bill|broadband|recharge|airtel|jio"),
        "transport" to Regex("irctc|railway|metro|bus|redbus|uber|ola|rapido"),
        "entertainment" to Regex("bookmyshow|pvr|inox|movie|hotstar"),
        "subscriptions" to Regex("amazon prime|netflix|spotify|youtube premium|icloud|prime video")
    )
    fun seedCategories() = names.mapIndexed { index, (id, name) ->
        com.khata.data.db.CategoryEntity(id, name, id, colors[id] ?: "muted", sortOrder = index)
    }
}

data class CategoryResult(val type: String, val categoryId: String, val confidence: Float, val source: String)

object Categorizer {
    fun normalize(raw: String): String {
        var value = raw.lowercase(Locale.ROOT).trim()
        listOf("upi-", "upi/", "pos ", "pos/", "imps/", "neft/", "rtgs/").forEach {
            value = value.replace(it, "")
        }
        value = value.replace(Regex("\\b\\d{6,}\\b"), "")
        value = value.replace(Regex("@(ybl|okaxis|paytm|apl)\\b"), "")
        return value.replace("*", " ").replace(Regex("\\s+"), " ").trim()
    }

    fun classify(raw: String, direction: String, rules: List<MerchantRuleEntity> = emptyList()): CategoryResult {
        val text = normalize(raw)
        val isCredit = direction == "CREDIT"
        val type = when {
            isCredit && Regex("salary|interest paid|int\\.pd|cashback credit").containsMatchIn(text) -> "INCOME"
            Regex("refund|reversal|chargeback").containsMatchIn(text) -> "REFUND"
            Regex("self transfer|to self|imps.*self|cc bill|credit card payment").containsMatchIn(text) -> "TRANSFER"
            Regex("zerodha|groww|upstox|kuvera|coin|indmoney|\\bsip\\b|mutual fund|\\bnps\\b|\\bppf\\b").containsMatchIn(text) -> "INVESTMENT"
            Regex("annual fee|amc|gst on|charges|penalty|late fee").containsMatchIn(text) -> "FEE"
            isCredit -> "INCOME"
            else -> "EXPENSE"
        }
        if (type != "EXPENSE") {
            val category = when (type) {
                "INCOME", "REFUND" -> "income"
                "TRANSFER" -> "transfers"
                "INVESTMENT" -> "investments"
                "FEE" -> "fees"
                else -> "uncategorized"
            }
            return CategoryResult(type, category, 0.98f, "KEYWORD")
        }
        rules.sortedByDescending { it.priority }.firstOrNull { rule ->
            val pattern = normalize(rule.pattern)
            when (rule.matchType) {
                "EXACT" -> text == pattern
                "STARTS_WITH" -> text.startsWith(pattern)
                "REGEX" -> runCatching { Regex(rule.pattern, RegexOption.IGNORE_CASE).containsMatchIn(text) }.getOrDefault(false)
                else -> text.contains(pattern)
            }
        }?.let { return CategoryResult(it.typeOverride ?: type, it.categoryId, 1f, if (it.isUserCreated) "LEARNED" else "MERCHANT") }

        val matched = KhataCategories.keywords.firstOrNull { (_, regex) -> regex.containsMatchIn(text) }
        return if (matched != null) CategoryResult(type, matched.first, 0.86f, "KEYWORD")
        else CategoryResult(type, "uncategorized", 0f, "NONE")
    }
}
