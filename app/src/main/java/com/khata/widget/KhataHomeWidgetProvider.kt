package com.khata.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import androidx.room.Room
import com.khata.R
import com.khata.data.db.KhataDatabase
import com.khata.data.db.MIGRATION_1_2
import com.khata.data.db.MIGRATION_2_3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

enum class WidgetPeriod(val key: String, val label: String, val caption: String) {
    DAY("DAY", "TODAY", "today"),
    WEEK("WEEK", "LAST 7 DAYS", "last 7 days"),
    MONTH("MONTH", "THIS MONTH", "this month"),
    ALL("ALL", "ALL TIME", "all time");

    fun dateRange(today: LocalDate): Pair<LocalDate, LocalDate> = when (this) {
        DAY -> today to today
        WEEK -> today.minusDays(6) to today
        MONTH -> today.withDayOfMonth(1) to today
        ALL -> LocalDate.of(1, 1, 1) to today
    }

    companion object {
        fun fromKey(key: String?): WidgetPeriod = entries.firstOrNull { it.key == key } ?: DAY
        fun fromAction(action: String?): WidgetPeriod? = entries.firstOrNull { period ->
            action == when (period) {
                DAY -> KhataHomeWidgetProvider.ACTION_DAY
                WEEK -> KhataHomeWidgetProvider.ACTION_WEEK
                MONTH -> KhataHomeWidgetProvider.ACTION_MONTH
                ALL -> KhataHomeWidgetProvider.ACTION_ALL
            }
        }
    }
}

internal enum class WidgetLayoutVariant { COMPACT, STANDARD, LARGE }

internal fun widgetLayoutVariantFor(widthDp: Int, heightDp: Int): WidgetLayoutVariant = when {
    widthDp in 1..239 || heightDp in 1..139 -> WidgetLayoutVariant.COMPACT
    widthDp >= 340 && heightDp >= 220 -> WidgetLayoutVariant.LARGE
    else -> WidgetLayoutVariant.STANDARD
}

class KhataHomeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        refresh(context, manager, ids, goAsync())
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        refresh(context, manager, intArrayOf(appWidgetId), goAsync())
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        preferences.edit().also { editor ->
            appWidgetIds.forEach { editor.remove(periodPreferenceKey(it)) }
        }.apply()
        super.onDeleted(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val selectedPeriod = WidgetPeriod.fromAction(intent?.action)
        if (selectedPeriod == null) {
            super.onReceive(context, intent)
            return
        }

        val widgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.w(TAG, "Ignoring widget period action without a valid app widget ID")
            return
        }

        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(periodPreferenceKey(widgetId), selectedPeriod.key).apply()
        val manager = AppWidgetManager.getInstance(context)
        refresh(context, manager, intArrayOf(widgetId), goAsync())
    }

    companion object {
        private const val TAG = "KhataHomeWidget"
        private const val PREFS = "khata_widget"
        internal const val ACTION_DAY = "com.khata.widget.PERIOD_DAY"
        internal const val ACTION_WEEK = "com.khata.widget.PERIOD_WEEK"
        internal const val ACTION_MONTH = "com.khata.widget.PERIOD_MONTH"
        internal const val ACTION_ALL = "com.khata.widget.PERIOD_ALL"

        private fun periodPreferenceKey(widgetId: Int) = "period_$widgetId"

        /** Called by app transaction mutations; the scheduled widget update remains a backup. */
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, KhataHomeWidgetProvider::class.java))
            if (ids.isNotEmpty()) refresh(context, manager, ids, null)
            WidgetRefresh.refreshInstalled(context)
        }

        private fun refresh(
            context: Context,
            manager: AppWidgetManager,
            ids: IntArray,
            pending: android.content.BroadcastReceiver.PendingResult?
        ) {
            if (ids.isEmpty()) {
                pending?.finish()
                return
            }
            val appContext = context.applicationContext
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                var database: KhataDatabase? = null
                try {
                    val db = Room.databaseBuilder(appContext, KhataDatabase::class.java, "khata.db")
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                    database = db
                    val today = LocalDate.now()
                    val preferences = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    val palette = WidgetTheme.resolve(appContext)
                    val updates = ids.map { widgetId ->
                        val period = WidgetPeriod.fromKey(preferences.getString(periodPreferenceKey(widgetId), null))
                        val (start, endInclusive) = period.dateRange(today)
                        val netSpent = db.dao().expenseTotalBetween(start.toString(), endInclusive.toString()).coerceAtLeast(0L)
                        val options = manager.getAppWidgetOptions(widgetId)
                        val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                        val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                        val variant = widgetLayoutVariantFor(width, height)
                        widgetId to render(appContext, widgetId, period, netSpent, variant, palette)
                    }
                    withContext(Dispatchers.Main) {
                        updates.forEach { (widgetId, views) -> manager.updateAppWidget(widgetId, views) }
                    }
                } catch (exception: Exception) {
                    Log.e(TAG, "Failed to refresh home-screen widget(s) ${ids.joinToString()}", exception)
                } finally {
                    database?.close()
                    pending?.finish()
                }
            }
        }

        private fun render(
            context: Context,
            widgetId: Int,
            period: WidgetPeriod,
            spent: Long,
            variant: WidgetLayoutVariant,
            palette: WidgetPalette
        ): RemoteViews {
            val layoutId = when (variant) {
                WidgetLayoutVariant.COMPACT -> R.layout.widget_khata_compact
                WidgetLayoutVariant.STANDARD -> R.layout.widget_khata
                WidgetLayoutVariant.LARGE -> R.layout.widget_khata_large
            }
            val views = RemoteViews(context.packageName, layoutId)
            views.setInt(R.id.widget_card, "setBackgroundResource", palette.cardBackground)
            val amount = NumberFormat.getIntegerInstance(Locale("en", "IN")).format(spent / 100)
            views.setTextViewText(R.id.widget_period_label, period.label)
            views.setTextColor(R.id.widget_period_label, palette.title)
            views.setTextViewText(R.id.widget_total, "₹$amount")
            views.setTextColor(R.id.widget_total, palette.primaryText)
            views.setTextViewText(
                R.id.widget_caption,
                if (spent == 0L) "Nothing spent ${period.caption}" else "net spent ${period.caption} · tap to open"
            )
            views.setTextColor(R.id.widget_caption, palette.secondaryText)

            val choices = listOf(
                Triple(WidgetPeriod.DAY, R.id.widget_period_day, ACTION_DAY),
                Triple(WidgetPeriod.WEEK, R.id.widget_period_week, ACTION_WEEK),
                Triple(WidgetPeriod.MONTH, R.id.widget_period_month, ACTION_MONTH),
                Triple(WidgetPeriod.ALL, R.id.widget_period_all, ACTION_ALL)
            )
            choices.forEachIndexed { index, (choice, viewId, action) ->
                val isActive = choice == period
                views.setTextColor(viewId, if (isActive) palette.activePeriodText else palette.idlePeriodText)
                views.setInt(viewId, "setBackgroundResource", if (isActive) palette.activePeriodBackground else palette.idlePeriodBackground)
                bindPeriodButton(context, views, widgetId, viewId, action, choice, index)
            }

            context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launchIntent ->
                val click = PendingIntent.getActivity(
                    context,
                    widgetId,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_card, click)
            }
            return views
        }

        private fun bindPeriodButton(
            context: Context,
            views: RemoteViews,
            widgetId: Int,
            viewId: Int,
            action: String,
            period: WidgetPeriod,
            index: Int
        ) {
            val intent = Intent(context, KhataHomeWidgetProvider::class.java).apply {
                this.action = action
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse("khata-widget://$widgetId/${period.key.lowercase(Locale.ROOT)}")
            }
            val click = PendingIntent.getBroadcast(
                context,
                widgetId * 10 + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(viewId, click)
        }
    }
}
