package com.blockcrush.puzzle.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.blockcrush.puzzle.BlockCrushApp
import com.blockcrush.puzzle.R
import com.blockcrush.puzzle.daily.DayClock
import com.blockcrush.puzzle.ui.HomeActivity

/** Posts the daily reminder, but only if the player has not played yet today. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = BlockCrushApp.get(context).prefs
        if (!prefs.reminderOn) return
        if (!ReminderScheduler.canPostNotifications(context)) return
        if (prefs.lastPlayedDay == DayClock.epochDay()) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        ReminderScheduler.ensureChannel(context)

        val open = Intent(context, HomeActivity::class.java).apply {
            putExtra(HomeActivity.EXTRA_OPEN_DAILY, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, ReminderScheduler.CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context).setPriority(Notification.PRIORITY_DEFAULT)
        }
        val notification = builder
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        nm.notify(ReminderScheduler.NOTIFICATION_ID, notification)
    }
}
