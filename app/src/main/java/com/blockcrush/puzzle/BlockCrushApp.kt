package com.blockcrush.puzzle

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.blockcrush.puzzle.ads.AdsManager
import com.blockcrush.puzzle.audio.MusicManager
import com.blockcrush.puzzle.audio.SoundManager
import com.blockcrush.puzzle.billing.BillingManager
import com.blockcrush.puzzle.notifications.ReminderScheduler

/** Application-wide singletons: preferences, audio, ads and billing. */
class BlockCrushApp : Application() {

    lateinit var prefs: Prefs
        private set
    lateinit var sounds: SoundManager
        private set
    lateinit var music: MusicManager
        private set
    lateinit var haptics: Haptics
        private set
    lateinit var ads: AdsManager
        private set
    lateinit var billing: BillingManager
        private set

    private var startedActivities = 0

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        sounds = SoundManager(this, prefs)
        music = MusicManager(this, prefs)
        haptics = Haptics(this, prefs)
        ads = AdsManager(this, prefs)
        billing = BillingManager(this, prefs)
        billing.connect()
        if (prefs.reminderOn) ReminderScheduler.schedule(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                if (startedActivities++ == 0) music.onAppForeground()
            }
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                if (--startedActivities <= 0) {
                    startedActivities = 0
                    music.onAppBackground()
                }
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    companion object {
        fun get(context: Context): BlockCrushApp = context.applicationContext as BlockCrushApp
    }
}
