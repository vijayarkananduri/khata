package com.khata.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.room.Room
import com.khata.R
import com.khata.data.db.KhataDatabase
import com.khata.data.db.MIGRATION_1_2
import com.khata.domain.stats.SpendPeriod
import com.khata.domain.stats.SpendPeriodRanges
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

class KhataHomeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        refresh(context, manager, ids, goAsync())
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        val period = when (action) {
            ACTION_DAY -> "DAY"
            ACTION_WEEK -> "WEEK"
            ACTION_MONTH -> "MONTH"
            else -> null
        }
        if (period == null) {
            super.onReceive(context, intent)
            return
        }
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PERIOD, period).apply()
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, KhataHomeWidgetProvider::class.java))
        if (ids.isNotEmpty()) refresh(context, manager, ids, goAsync())
    }

    companion object {
        private const val PREFS = "khata_widget"
        private const val KEY_PERIOD = "period"
        private const val ACTION_DAY = "com.khata.widget.PERIOD_DAY"
        private const val ACTION_WEEK = "com.khata.widget.PERIOD_WEEK"
        private const val ACTION_MONTH = "com.khata.widget.PERIOD_MONTH"

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, KhataHomeWidgetProvider::class.java))
            if (ids.isNotEmpty()) refresh(context, manager, ids, null)
        }

        private fun refresh(context: Context, manager: AppWidgetManager, ids: IntArray, pending: android.content.BroadcastReceiver.PendingResult?) {
            val appContext = context.applicationContext
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                var db: KhataDatabase? = null
                try {
                    val database = Room.databaseBuilder(appContext, KhataDatabase::class.java, "khata.db")
                        .addMigrations(MIGRATION_1_2).build()
                    db = database
                    val today = LocalDate.now()
                    val key = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .getString(KEY_PERIOD, "DAY") ?: "DAY"
                    val period = SpendPeriod.fromKey(key)
                    val range = SpendPeriodRanges.current(period, today)
                    val spent = database.dao().expenseTotalBetween(range.start.toString(), range.endInclusive.toString())
                    withContext(Dispatchers.Main) {
                        val views = RemoteViews(appContext.packageName, R.layout.widget_khata)
                        val amount = NumberFormat.getIntegerInstance(Locale("en", "IN")).format(spent / 100)
                        val label = when (period) { SpendPeriod.WEEK -> "LAST 7 DAYS"; SpendPeriod.MONTH -> "THIS MONTH"; else -> "TODAY" }
                        val lowerLabel = when (period) { SpendPeriod.WEEK -> "last 7 days"; SpendPeriod.MONTH -> "this month"; else -> "today" }
                        views.setTextViewText(R.id.widget_period_label, label)
                        views.setTextViewText(R.id.widget_total, "₹$amount")
                        views.setTextViewText(R.id.widget_caption, if (spent == 0L) "Nothing spent $lowerLabel" else "spent $lowerLabel · tap to open")
                        val chosenColor = android.graphics.Color.WHITE
                        val idleColor = android.graphics.Color.rgb(190, 199, 196)
                        views.setTextColor(R.id.widget_period_day, if (period == SpendPeriod.DAY) chosenColor else idleColor)
                        views.setTextColor(R.id.widget_period_week, if (period == SpendPeriod.WEEK) chosenColor else idleColor)
                        views.setTextColor(R.id.widget_period_month, if (period == SpendPeriod.MONTH) chosenColor else idleColor)
                        views.setInt(R.id.widget_period_day, "setBackgroundResource", if (period == SpendPeriod.DAY) R.drawable.widget_period_active else R.drawable.widget_period_idle)
                        views.setInt(R.id.widget_period_week, "setBackgroundResource", if (period == SpendPeriod.WEEK) R.drawable.widget_period_active else R.drawable.widget_period_idle)
                        views.setInt(R.id.widget_period_month, "setBackgroundResource", if (period == SpendPeriod.MONTH) R.drawable.widget_period_active else R.drawable.widget_period_idle)
                        bindPeriodButton(appContext, views, R.id.widget_period_day, ACTION_DAY, 11)
                        bindPeriodButton(appContext, views, R.id.widget_period_week, ACTION_WEEK, 12)
                        bindPeriodButton(appContext, views, R.id.widget_period_month, ACTION_MONTH, 13)
                        appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)?.let { open ->
                            val click = PendingIntent.getActivity(appContext, 0, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            views.setOnClickPendingIntent(R.id.widget_card, click)
                        }
                        manager.updateAppWidget(ids, views)
                    }
                } catch (_: Exception) {
                    // Keep the prior widget content if local storage is temporarily unavailable.
                } finally {
                    db?.close()
                    pending?.finish()
                }
            }
        }

        private fun bindPeriodButton(context: Context, views: RemoteViews, viewId: Int, action: String, requestCode: Int) {
            val intent = Intent(context, KhataHomeWidgetProvider::class.java).apply {
                this.action = action
                data = Uri.parse("khata-widget://$requestCode")
            }
            val click = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(viewId, click)
        }
    }
}
