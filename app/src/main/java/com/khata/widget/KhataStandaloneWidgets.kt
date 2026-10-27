package com.khata.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.widget.RemoteViews
import androidx.room.Room
import com.khata.MainActivity
import com.khata.R
import com.khata.data.db.KhataDatabase
import com.khata.data.db.MIGRATION_1_2
import com.khata.data.db.MIGRATION_2_3
import com.khata.domain.stats.budgetProgressFraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

enum class StandaloneWidgetKind(val title: String, val period: WidgetPeriod?) {
    DAY("TODAY", WidgetPeriod.DAY),
    WEEK("LAST 7 DAYS", WidgetPeriod.WEEK),
    MONTH("THIS MONTH", WidgetPeriod.MONTH),
    ALL("ALL TIME", WidgetPeriod.ALL),
    RING("MONTHLY BUDGET", WidgetPeriod.MONTH),
    QUOTE("QUOTE", null)
}

abstract class KhataStandaloneWidgetProvider : AppWidgetProvider() {
    protected abstract val kind: StandaloneWidgetKind

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        WidgetRefresh.refresh(context, manager, ids, kind, goAsync())
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        WidgetRefresh.refresh(context, manager, intArrayOf(appWidgetId), kind, goAsync())
    }
}

class KhataDailyWidgetProvider : KhataStandaloneWidgetProvider() { override val kind = StandaloneWidgetKind.DAY }
class KhataWeeklyWidgetProvider : KhataStandaloneWidgetProvider() { override val kind = StandaloneWidgetKind.WEEK }
class KhataMonthlyWidgetProvider : KhataStandaloneWidgetProvider() { override val kind = StandaloneWidgetKind.MONTH }
class KhataAllTimeWidgetProvider : KhataStandaloneWidgetProvider() { override val kind = StandaloneWidgetKind.ALL }
class KhataProgressRingWidgetProvider : KhataStandaloneWidgetProvider() { override val kind = StandaloneWidgetKind.RING }
class KhataQuoteWidgetProvider : KhataStandaloneWidgetProvider() { override val kind = StandaloneWidgetKind.QUOTE }

internal object WidgetRefresh {
    private const val TAG = "KhataWidgets"
    private const val PREFS = "khata_widget_prefs"
    private const val QUOTE_PREFS = "khata_quote_history"

    fun refresh(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray,
        kind: StandaloneWidgetKind,
        pending: android.content.BroadcastReceiver.PendingResult? = null
    ) {
        if (ids.isEmpty()) { pending?.finish(); return }
        val app = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var db: KhataDatabase? = null
            try {
                val spentAndBudget = if (kind == StandaloneWidgetKind.QUOTE) 0L to 0L else {
                    val database = Room.databaseBuilder(app, KhataDatabase::class.java, "khata.db")
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                    db = database
                    val (from, to) = requireNotNull(kind.period).dateRange(LocalDate.now())
                    database.dao().expenseTotalBetween(from.toString(), to.toString()).coerceAtLeast(0L) to
                        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("budget_paise", 0L).coerceAtLeast(0L)
                }
                val palette = WidgetTheme.resolve(app)
                val viewsById = ids.map { id -> id to render(app, id, kind, spentAndBudget.first, spentAndBudget.second, palette) }
                withContext(Dispatchers.Main) { viewsById.forEach { (id, views) -> manager.updateAppWidget(id, views) } }
            } catch (error: Exception) {
                Log.e(TAG, "Widget refresh failed (${kind.name}, IDs=${ids.joinToString()})", error)
            } finally {
                runCatching { db?.close() }.onFailure { Log.w(TAG, "Could not close widget database", it) }
                runCatching { pending?.finish() }.onFailure { Log.w(TAG, "Could not finish widget broadcast", it) }
            }
        }
    }

    fun refreshInstalled(context: Context) {
        val app = context.applicationContext
        val manager = AppWidgetManager.getInstance(app)
        val providers = listOf(
            KhataDailyWidgetProvider::class.java to StandaloneWidgetKind.DAY,
            KhataWeeklyWidgetProvider::class.java to StandaloneWidgetKind.WEEK,
            KhataMonthlyWidgetProvider::class.java to StandaloneWidgetKind.MONTH,
            KhataAllTimeWidgetProvider::class.java to StandaloneWidgetKind.ALL,
            KhataProgressRingWidgetProvider::class.java to StandaloneWidgetKind.RING,
            KhataQuoteWidgetProvider::class.java to StandaloneWidgetKind.QUOTE
        )
        providers.forEach { (provider, kind) ->
            val ids = runCatching { manager.getAppWidgetIds(ComponentName(app, provider)) }
                .onFailure { Log.w(TAG, "Could not enumerate ${provider.simpleName}", it) }
                .getOrDefault(intArrayOf())
            if (ids.isNotEmpty()) refresh(app, manager, ids, kind)
        }
    }

    private fun render(context: Context, widgetId: Int, kind: StandaloneWidgetKind, spent: Long, budget: Long, palette: WidgetPalette): RemoteViews = when (kind) {
        StandaloneWidgetKind.RING -> renderRing(context, widgetId, spent, budget, palette)
        StandaloneWidgetKind.QUOTE -> renderQuote(context, widgetId, palette)
        else -> renderSpend(context, widgetId, kind, spent, palette)
    }

    private fun renderSpend(context: Context, widgetId: Int, kind: StandaloneWidgetKind, spent: Long, palette: WidgetPalette): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_spend_preset)
        views.setInt(R.id.preset_card, "setBackgroundResource", palette.cardBackground)
        val (from, to) = requireNotNull(kind.period).dateRange(LocalDate.now())
        val amount = formatMoney(spent)
        views.setTextViewText(R.id.preset_title, kind.title)
        views.setTextColor(R.id.preset_title, palette.title)
        views.setTextViewText(R.id.preset_total, amount)
        views.setTextColor(R.id.preset_total, palette.primaryText)
        views.setTextViewText(R.id.preset_caption, when (kind) {
            StandaloneWidgetKind.DAY -> "spent today · ${LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))}"
            StandaloneWidgetKind.WEEK -> "from ${from.format(java.time.format.DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))}"
            StandaloneWidgetKind.MONTH -> "since ${from.format(java.time.format.DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))}"
            else -> "recorded on this device"
        })
        views.setTextColor(R.id.preset_caption, palette.secondaryText)
        bindOpenApp(context, views, R.id.preset_card, widgetId)
        return views
    }

    private fun renderRing(context: Context, widgetId: Int, spent: Long, budget: Long, palette: WidgetPalette): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_progress_ring)
        views.setInt(R.id.widget_ring_card, "setBackgroundResource", palette.cardBackground)
        val ratio = budgetProgressFraction(spent, budget)
        views.setImageViewBitmap(R.id.widget_ring_image, createRingBitmap(ratio, palette))
        views.setTextViewText(R.id.widget_ring_percent, if (budget > 0L) "${(ratio * 100f).toInt()}%" else "—")
        views.setTextColor(R.id.widget_ring_percent, palette.primaryText)
        views.setTextColor(R.id.widget_ring_title, palette.title)
        views.setTextViewText(R.id.widget_ring_total, formatMoney(spent))
        views.setTextColor(R.id.widget_ring_total, palette.primaryText)
        views.setTextViewText(R.id.widget_ring_caption, if (budget > 0L) "of ${formatMoney(budget)} monthly budget" else "Set a monthly budget in Khata")
        views.setTextColor(R.id.widget_ring_caption, palette.secondaryText)
        bindOpenApp(context, views, R.id.widget_ring_card, widgetId)
        return views
    }

    private fun createRingBitmap(progress: Float, palette: WidgetPalette): Bitmap {
        val bitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bounds = RectF(15f, 15f, 145f, 145f)
        val track = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.ringTrack
            style = Paint.Style.STROKE
            strokeWidth = 13f
            strokeCap = Paint.Cap.ROUND
        }
        val arc = Paint(track).apply { color = palette.ringProgress }
        canvas.drawArc(bounds, -90f, 360f, false, track)
        if (progress > 0f) canvas.drawArc(bounds, -90f, (359.6f * progress.coerceIn(0f, 1f)), false, arc)
        return bitmap
    }

    private fun renderQuote(context: Context, widgetId: Int, palette: WidgetPalette): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_quote)
        views.setInt(R.id.widget_quote_card, "setBackgroundResource", palette.cardBackground)
        val shared = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val language = shared.getString("language", "te") ?: "te"
        val roman = shared.getBoolean("roman_script", false)
        val quote = runCatching {
            val catalog = context.resources.openRawResource(R.raw.quotes).bufferedReader().use { JSONObject(it.readText()) }
            val lang = catalog.optJSONObject("languages")?.optJSONObject(language)
            val array = lang?.optJSONArray("quotes") ?: lang?.optJSONArray("splashQuotes")
            if (array == null || array.length() == 0) return@runCatching null
            val all = (0 until array.length()).mapNotNull { index -> runCatching { array.getJSONObject(index) }.getOrNull() }
            val prefs = context.getSharedPreferences(QUOTE_PREFS, Context.MODE_PRIVATE)
            val day = LocalDate.now().toString()
            val dateKey = "widget_date_$language"
            val seenKey = "widget_seen_$language"
            val previouslySeen = if (prefs.getString(dateKey, null) == day) prefs.getStringSet(seenKey, emptySet()).orEmpty() else emptySet()
            val seen = previouslySeen.toMutableSet()
            val unseen = all.filter { it.optString("id") !in seen }
            val candidates = unseen.ifEmpty { all }
            val chosen = candidates.shuffled().firstOrNull() ?: return@runCatching null
            if (unseen.isEmpty()) seen.clear()
            seen += chosen.optString("id")
            prefs.edit().putString(dateKey, day).putStringSet(seenKey, seen).apply()
            chosen.optString(if (roman) "roman" else "native").takeIf { it.isNotBlank() }
        }.onFailure { Log.w(TAG, "Quote widget used an empty fallback", it) }.getOrNull()
        views.setTextViewText(R.id.widget_quote_text, quote ?: "Open Khata for a fresh line.")
        views.setTextColor(R.id.widget_quote_text, palette.primaryText)
        bindOpenApp(context, views, R.id.widget_quote_card, widgetId)
        return views
    }

    private fun bindOpenApp(context: Context, views: RemoteViews, viewId: Int, widgetId: Int) {
        val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            context, 50_000 + widgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(viewId, pending)
    }

    private fun formatMoney(paise: Long): String = "₹" + NumberFormat.getIntegerInstance(Locale("en", "IN")).format((paise.coerceAtLeast(0L) / 100L))
}
