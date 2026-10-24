package com.khata.domain.quotes

import android.content.Context
import com.khata.R
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class InAppQuote(val text: String)

@Singleton
class QuoteRepository @Inject constructor(private val context: Context) {
    private val catalog: JSONObject by lazy {
        context.resources.openRawResource(R.raw.quotes).bufferedReader().use { JSONObject(it.readText()) }
    }
    private val history = context.getSharedPreferences("khata_quote_history", Context.MODE_PRIVATE)

    fun splashText(language: String, roman: Boolean): String {
        val lang = catalog.optJSONObject("languages")?.optJSONObject(language) ?: return ""
        val array = lang.optJSONArray("splashQuotes")
        if (array == null || array.length() == 0) return ""
        val chosen = choose(language, (0 until array.length()).map { array.getJSONObject(it) })
            ?: return ""
        return chosen.optString(if (roman) "roman" else "native")
    }

    fun forTrigger(language: String, trigger: String, roman: Boolean): InAppQuote? {
        val languageNode = catalog.optJSONObject("languages")?.optJSONObject(language) ?: return null
        val quotes = languageNode.optJSONArray("quotes") ?: return null
        val all = (0 until quotes.length()).map { quotes.getJSONObject(it) }
        val selected = (all.filter { it.optString("trigger") == trigger } + all.filter { it.optString("trigger") == "DAILY_NOTE" })
            .distinctBy { it.optString("id") }
        if (selected.isEmpty()) return null
        val item = choose(language, selected) ?: return null
        return InAppQuote(item.optString(if (roman) "roman" else "native"))
    }

    private fun choose(language: String, choices: List<JSONObject>): JSONObject? {
        val today = LocalDate.now()
        val key = "recent_$language"
        val stored = runCatching { JSONArray(history.getString(key, "[]")) }.getOrDefault(JSONArray())
        val kept = (0 until stored.length()).mapNotNull { index ->
            runCatching { stored.getJSONObject(index) }.getOrNull()
        }.filter { item ->
            runCatching { LocalDate.parse(item.getString("day")) == today }.getOrDefault(false)
        }
        val recentIds = kept.map { it.optString("id") }.toSet()
        val fresh = choices.filter { it.optString("id") !in recentIds }
        if (fresh.isEmpty()) return null
        val chosen = fresh.shuffled().first()
        val next = JSONArray()
        (kept + JSONObject().put("id", chosen.optString("id")).put("day", today.toString())).takeLast(40).forEach { next.put(it) }
        history.edit().putString(key, next.toString()).apply()
        return chosen
    }
}
