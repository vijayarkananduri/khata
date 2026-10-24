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
    private const val CHANNEL_NAME = "Budget alerts"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "User-requested spending and budget alerts from Khata"
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
