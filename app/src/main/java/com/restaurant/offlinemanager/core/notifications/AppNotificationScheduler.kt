package com.restaurant.offlinemanager.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AppNotificationScheduler {
    private const val PERIODIC_WORK_NAME = "important_offline_alerts_periodic"
    private const val ONE_TIME_WORK_NAME = "important_offline_alerts_now"

    fun schedule(context: Context) {
        val appContext = context.applicationContext
        AppNotificationManager.ensureChannels(appContext)

        val periodicRequest = PeriodicWorkRequestBuilder<ImportantAlertsWorker>(
            6,
            TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )

        runOnce(appContext)
    }

    fun runOnce(context: Context) {
        val appContext = context.applicationContext
        val request = OneTimeWorkRequestBuilder<ImportantAlertsWorker>().build()
        WorkManager.getInstance(appContext).enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancel(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
        workManager.cancelUniqueWork(ONE_TIME_WORK_NAME)
        AppNotificationManager.clearImportantAlertSignature(context.applicationContext)
    }
}
