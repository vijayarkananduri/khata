package com.khata.domain.quotes

import android.content.Context
import com.khata.R
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

data class InAppQuote(val text: String)

internal data class AmbientQuoteCandidate(
    val id: String,
    val tone: String,
    val lane: String,
    val native: String,
    val roman: String
)

internal object AmbientQuotePicker {
    fun currentLane(now: LocalDateTime): String = when {
        now.dayOfMonth in 1..3 -> "payday"
        now.dayOfMonth >= 28 -> "month_end"
        now.hour in 5..11 -> "morning"
        now.hour in 12..16 -> "afternoon"
        now.hour in 17..20 -> "evening"
        else -> "night"
    }

    fun choose(
        all: List<AmbientQuoteCandidate>,
        lane: String,
        lastTone: String?,
        lastId: String?
    ): AmbientQuoteCandidate? {
        val laneQuotes = all.filter { it.lane == lane }.ifEmpty { all }
        val candidates = laneQuotes
            .filter { it.id != lastId }
            .filter { lastTone == null || it.tone != lastTone }
            .ifEmpty { laneQuotes.filter { it.id != lastId } }
            .ifEmpty { laneQuotes }
        return candidates.shuffled().firstOrNull()
    }
}

object QuoteRotation {
    // CHANGED: 12h → 24h (one quote per trigger per day)
    const val DEFAULT_INTERVAL_MS = 24 * 60 * 60 * 1000L

    fun shouldRotate(
        lastShownAtMillis: Long,
        nowMillis: Long,
        intervalMillis: Long = DEFAULT_INTERVAL_MS
    ): Boolean =
        lastShownAtMillis <= 0L ||
        nowMillis < lastShownAtMillis ||
        nowMillis - lastShownAtMillis >= intervalMillis
}

@Singleton
class QuoteRepository @Inject constructor(private val context: Context) {

    private val catalog: JSONObject by lazy {
        context.resources.openRawResource(R.raw.quotes)
            .bufferedReader()
            .use { JSONObject(it.readText()) }
    }

    private val history =
        context.getSharedPreferences("khata_quote_history", Context.MODE_PRIVATE)

    private val chooseLock = Any()

    fun splashText(language: String, roman: Boolean): String {
        val lang = catalog.optJSONObject("languages")
            ?.optJSONObject(language) ?: return ""
        val array = lang.optJSONArray("splashQuotes")
        if (array == null || array.length() == 0) return ""
        val chosen = choose(
            language,
            (0 until array.length()).map { array.getJSONObject(it) }
        ) ?: return ""
        return chosen.optString(if (roman) "roman" else "native")
    }

    fun forTrigger(language: String, trigger: String, roman: Boolean): InAppQuote? {
        val languageNode = catalog.optJSONObject("languages")
            ?.optJSONObject(language) ?: return null
        val quotes = languageNode.optJSONArray("quotes") ?: return null

        val all = (0 until quotes.length()).map { quotes.getJSONObject(it) }
        val selected = (
            all.filter { it.optString("trigger") == trigger } +
            all.filter { it.optString("trigger") == "DAILY_NOTE" }
        ).distinctBy { it.optString("id") }

        if (selected.isEmpty()) return null

        // CHANGED: active key is now per (language + trigger), not per language
        val idKey   = "active_id_${language}_$trigger"
        val timeKey = "active_at_${language}_$trigger"

        val activeId = history.getString(idKey, null)
        val activeAt = history.getLong(timeKey, 0L)
        val active   = all.firstOrNull { it.optString("id") == activeId }
        val now      = System.currentTimeMillis()

        if (active != null && !QuoteRotation.shouldRotate(activeAt, now)) {
            return InAppQuote(active.optString(if (roman) "roman" else "native"))
        }

        val item = choose(language, selected) ?: active ?: selected.first()
        history.edit()
            .putString(idKey, item.optString("id"))
            .putLong(timeKey, now)
            .apply()

        return InAppQuote(item.optString(if (roman) "roman" else "native"))
    }

    fun ambientQuote(language: String, roman: Boolean): InAppQuote? {
        val languageNode = catalog.optJSONObject("languages")?.optJSONObject(language) ?: return null
        val ambient = languageNode.optJSONArray("ambient") ?: return null
        if (ambient.length() == 0) return null

        val all = (0 until ambient.length()).mapNotNull { index ->
            val quote = ambient.optJSONObject(index) ?: return@mapNotNull null
            val id = quote.optString("id").trim()
            val native = quote.optString("native").trim()
            if (id.isBlank() || native.isBlank()) return@mapNotNull null
            AmbientQuoteCandidate(
                id = id,
                tone = quote.optString("tone", "neutral"),
                lane = quote.optString("lane"),
                native = native,
                roman = quote.optString("roman").ifBlank { native }
            )
        }
        if (all.isEmpty()) return null

        return synchronized(chooseLock) {
            val lane = AmbientQuotePicker.currentLane(LocalDateTime.now())
            val chosen = AmbientQuotePicker.choose(
                all = all,
                lane = lane,
                lastTone = history.getString("last_ambient_tone_$language", null),
                lastId = history.getString("last_ambient_id_$language", null)
            ) ?: return@synchronized null

            history.edit()
                .putString("last_ambient_tone_$language", chosen.tone)
                .putString("last_ambient_id_$language", chosen.id)
                .apply()

            InAppQuote(if (roman) chosen.roman else chosen.native)
        }
    }

    private fun choose(language: String, choices: List<JSONObject>): JSONObject? =
        synchronized(chooseLock) {
            val today = LocalDate.now()
            val key = "recent_$language"

            val stored = runCatching {
                JSONArray(history.getString(key, "[]"))
            }.getOrDefault(JSONArray())

            val kept = (0 until stored.length()).mapNotNull { index ->
                runCatching { stored.getJSONObject(index) }.getOrNull()
            }.filter { item ->
                runCatching {
                    LocalDate.parse(item.getString("day")) == today
                }.getOrDefault(false)
            }

            val recentIds = kept.map { it.optString("id") }.toSet()
            val fresh = choices.filter { it.optString("id") !in recentIds }
            if (fresh.isEmpty()) return null

            val chosen = fresh.shuffled().first()

            val next = JSONArray()
            (kept + JSONObject()
                .put("id", chosen.optString("id"))
                .put("day", today.toString()))
                .takeLast(40)
                .forEach { next.put(it) }

            history.edit().putString(key, next.toString()).apply()
            chosen
        }
}
