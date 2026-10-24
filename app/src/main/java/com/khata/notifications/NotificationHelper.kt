package com.khata.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.khata.R

object NotificationHelper {
    private const val CHANNEL_ID = "khata_budget"
    private const val CHANNEL_NAME = "Khata reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Budget and opt-in restock reminders from Khata"
            })
        }
    }

    fun canNotify(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    fun showBudgetExceeded(context: Context, spentPaise: Long, budgetPaise: Long) {
        show(context, 1001, "Monthly budget reached", "You've spent ₹${format(spentPaise)} against your ₹${format(budgetPaise)} budget.")
    }

    fun showRestockNudge(context: Context, item: String, category: String, intervalDays: Long, id: Int) {
        val label = when (category) { "groceries" -> "groceries"; "fuel" -> "fuel"; "health" -> "essentials"; else -> "supplies" }
        show(context, 2000 + kotlin.math.abs(id % 100000), "Time to check $item?", "Based on your usual $intervalDays-day restock rhythm, you may be running low. Check your $label before buying.")
    }

    fun showPermissionSuccess(context: Context) {
        show(context, 1002, "Budget alerts are on", "Khata will notify you when a recorded expense takes you over your monthly budget.")
    }

    private fun show(context: Context, id: Int, title: String, message: String) {
        createChannel(context)
        if (!canNotify(context)) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private fun format(paise: Long): String = java.text.NumberFormat.getIntegerInstance(java.util.Locale("en", "IN")).format(paise / 100)
}
