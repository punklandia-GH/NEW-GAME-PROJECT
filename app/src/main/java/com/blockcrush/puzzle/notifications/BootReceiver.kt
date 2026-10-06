package com.blockcrush.puzzle.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blockcrush.puzzle.BlockCrushApp

/** Alarms do not survive a reboot, so re-schedule the reminder if it is enabled. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (BlockCrushApp.get(context).prefs.reminderOn) ReminderScheduler.schedule(context)
    }
}
