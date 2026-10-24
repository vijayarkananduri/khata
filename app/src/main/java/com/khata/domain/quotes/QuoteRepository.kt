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
    fun splashText(): String = catalog.getJSONObject("splash").getString("text")
    fun forTrigger(language: String, trigger: String, roman: Boolean): InAppQuote? {
        val languageNode = catalog.optJSONObject("languages")?.optJSONObject(language) ?: return null
        val quotes = languageNode.optJSONArray("quotes") ?: return null
        for (index in 0 until quotes.length()) {
            val item = quotes.getJSONObject(index)
            if (item.optString("trigger") == trigger) {
                return InAppQuote(item.optString(if (roman) "roman" else "native"), item.optString("subtitle"), trigger)
            }
        }
        return null
    }
}
