package com.khata.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.room.Room
import com.khata.R
import com.khata.data.db.KhataDatabase
import com.khata.data.db.MIGRATION_1_2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

class KhataHomeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = refresh(context, manager, ids, goAsync())

    companion object {
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
                    val database = Room.databaseBuilder(appContext, KhataDatabase::class.java, "khata.db").addMigrations(MIGRATION_1_2).build()
                    db = database
                    val spent = database.dao().todayExpenseTotal(LocalDate.now().toString())
                    withContext(Dispatchers.Main) {
                        val views = RemoteViews(appContext.packageName, R.layout.widget_khata)
                        val rupees = NumberFormat.getIntegerInstance(Locale("en", "IN")).format(spent / 100)
                        views.setTextViewText(R.id.widget_total, "₹$rupees")
                        views.setTextViewText(R.id.widget_caption, if (spent == 0L) "Nothing spent today" else "spent today · tap to open")
                        val intent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
                        intent?.let {
                            val click = android.app.PendingIntent.getActivity(appContext, 0, it, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
                            views.setOnClickPendingIntent(R.id.widget_card, click)
                        }
                        manager.updateAppWidget(ids, views)
                    }
                } catch (_: Exception) {
                    // Keep the previous widget content if local storage is temporarily unavailable.
                } finally { db?.close(); pending?.finish() }
            }
        }


    }
}
