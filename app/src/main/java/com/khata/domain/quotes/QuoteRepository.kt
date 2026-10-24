package com.khata.domain.quotes

import android.content.Context
import com.khata.R
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class InAppQuote(val text: String, val subtitle: String, val trigger: String)

@Singleton
class QuoteRepository @Inject constructor(private val context: Context) {
    private val catalog: JSONObject by lazy {
        context.resources.openRawResource(R.raw.quotes).bufferedReader().use { JSONObject(it.readText()) }
    }
    fun splashText(language: String, roman: Boolean): String {
        val lang = catalog.optJSONObject("languages")?.optJSONObject(language)
        val splash = lang?.optJSONObject("splash")
        val field = if (roman) "roman" else "native"
        return splash?.optString(field)?.takeIf { it.isNotBlank() } ?: "Your next good money choice starts here."
    }
    fun forTrigger(language: String, trigger: String, roman: Boolean): InAppQuote? {
        val languageNode = catalog.optJSONObject("languages")?.optJSONObject(language) ?: return null
        val quotes = languageNode.optJSONArray("quotes") ?: return null
        val matches = (0 until quotes.length()).map { quotes.getJSONObject(it) }.filter { it.optString("trigger") == trigger }
        val fallback = if (matches.isNotEmpty()) matches else (0 until quotes.length()).map { quotes.getJSONObject(it) }.filter { it.optString("trigger") == "DAILY_NOTE" }
        if (fallback.isEmpty()) return null
        val item = fallback[java.time.LocalDate.now().dayOfYear % fallback.size]
        return InAppQuote(item.optString(if (roman) "roman" else "native"), item.optString("subtitle"), trigger)
    }
}
