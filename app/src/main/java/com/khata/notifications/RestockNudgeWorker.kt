package com.khata.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.room.Room
import com.khata.data.db.KhataDatabase
import com.khata.data.db.MIGRATION_1_2
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class RestockNudgeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!applicationContext.getSharedPreferences("khata_nudge_worker", Context.MODE_PRIVATE).getBoolean("enabled", true)) return Result.success()
        val db = Room.databaseBuilder(applicationContext, KhataDatabase::class.java, "khata.db").addMigrations(MIGRATION_1_2).build()
        return try {
            val now = LocalDate.now()
            NudgeCadence.due(db.dao().nudgePurchases(), now).forEach { nudge ->
                val prefs = applicationContext.getSharedPreferences("khata_nudge_worker", Context.MODE_PRIVATE)
                val notificationKey = "sent_${nudge.key}"
                if (NotificationHelper.canNotify(applicationContext) && prefs.getString(notificationKey, null) != nudge.dueDate.toString()) {
                    NotificationHelper.showRestockNudge(applicationContext, nudge.item, nudge.category, nudge.intervalDays, nudge.key.hashCode())
                    prefs.edit().putString(notificationKey, nudge.dueDate.toString()).apply()
                }
            }
            Result.success()
        } catch (_: Exception) { Result.retry() } finally { db.close() }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RestockNudgeWorker>(24, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build()).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("khata_restock_nudges", ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
