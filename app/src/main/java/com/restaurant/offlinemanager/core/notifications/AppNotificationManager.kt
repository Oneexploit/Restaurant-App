package com.restaurant.offlinemanager.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.restaurant.offlinemanager.MainActivity
import com.restaurant.offlinemanager.R

object AppNotificationManager {
    private const val IMPORTANT_CHANNEL_ID = "important_alerts"
    private const val IMPORTANT_NOTIFICATION_ID = 1101
    private const val PREFS_NAME = "important_notification_state"
    private const val KEY_LAST_SIGNATURE = "last_signature"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            IMPORTANT_CHANNEL_ID,
            "هشدارهای مهم",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "یادآوری‌های مهم آفلاین مثل کمبود موجودی، مطالبات و بدهی‌ها"
            enableVibration(true)
        }

        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    fun hasRuntimePermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun canPostNotifications(context: Context): Boolean =
        hasRuntimePermission(context) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    @SuppressLint("MissingPermission")
    fun showImportantAlert(
        context: Context,
        title: String,
        message: String,
        signature: String
    ) {
        if (!canPostNotifications(context)) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_LAST_SIGNATURE, null) == signature) return

        ensureChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, IMPORTANT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(Color.rgb(214, 168, 79))
            .setContentTitle(title)
            .setContentText(message.lineSequence().firstOrNull().orEmpty())
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        NotificationManagerCompat.from(context).notify(IMPORTANT_NOTIFICATION_ID, notification)
        prefs.edit().putString(KEY_LAST_SIGNATURE, signature).apply()
    }

    fun clearImportantAlertSignature(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LAST_SIGNATURE)
            .apply()
    }

    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(android.net.Uri.parse("package:${context.packageName}"))
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(intent) }
    }
}
