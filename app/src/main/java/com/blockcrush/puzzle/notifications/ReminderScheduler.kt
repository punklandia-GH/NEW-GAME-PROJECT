package com.blockcrush.puzzle.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.blockcrush.puzzle.R
import java.util.Calendar
import java.util.TimeZone

/**
 * Schedules one gentle reminder per day (around 7 pm local time) using an inexact alarm.
 * Nothing is scheduled unless the player turned the reminder on in Settings.
 */
object ReminderScheduler {

    const val CHANNEL_ID = "daily_reminder"
    const val NOTIFICATION_ID = 1
    const val REMINDER_HOUR = 19
    private const val REQUEST_CODE = 1001

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val trigger = nextTriggerMillis(System.currentTimeMillis(), TimeZone.getDefault())
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            trigger,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context)
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(pendingIntent(context))
    }

    /** The next [REMINDER_HOUR]:00 local time strictly after [now]. */
    fun nextTriggerMillis(now: Long, zone: TimeZone): Long {
        val cal = Calendar.getInstance(zone)
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= now) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = context.getString(R.string.notification_channel_description)
        nm.createNotificationChannel(channel)
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
