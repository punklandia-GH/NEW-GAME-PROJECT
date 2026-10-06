package com.blockcrush.puzzle.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.blockcrush.puzzle.Prefs
import com.blockcrush.puzzle.R

/**
 * Loops the relaxing background track while the app is in the foreground.
 * It stays quiet when another app is already playing music, and respects the music setting.
 */
class MusicManager(private val context: Context, private val prefs: Prefs) {

    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var inForeground = false
    private var currentVolume = 0f

    private val fadeStep = object : Runnable {
        override fun run() {
            val p = player ?: return
            if (currentVolume < TARGET_VOLUME) {
                currentVolume = (currentVolume + 0.04f).coerceAtMost(TARGET_VOLUME)
                try {
                    p.setVolume(currentVolume, currentVolume)
                } catch (e: IllegalStateException) {
                    return
                }
                handler.postDelayed(this, 60L)
            }
        }
    }

    fun onAppForeground() {
        inForeground = true
        if (prefs.musicOn) start()
    }

    fun onAppBackground() {
        inForeground = false
        pause()
    }

    /** Call after the music setting changes. */
    fun onSettingChanged() {
        if (prefs.musicOn && inForeground) start() else pause()
    }

    private fun start() {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (player == null && am != null && am.isMusicActive) return // don't talk over the player's own music
        val p = player ?: createPlayer() ?: return
        try {
            if (!p.isPlaying) {
                currentVolume = 0f
                p.setVolume(0f, 0f)
                p.start()
                handler.removeCallbacks(fadeStep)
                handler.post(fadeStep)
            }
        } catch (e: IllegalStateException) {
            release()
        }
    }

    private fun pause() {
        handler.removeCallbacks(fadeStep)
        val p = player ?: return
        try {
            if (p.isPlaying) p.pause()
        } catch (e: IllegalStateException) {
            release()
        }
    }

    private fun createPlayer(): MediaPlayer? {
        return try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val p = MediaPlayer.create(context, R.raw.music_calm, attrs, 0) ?: return null
            p.isLooping = true
            p.setOnErrorListener { _, _, _ ->
                release()
                true
            }
            player = p
            p
        } catch (e: RuntimeException) {
            null
        }
    }

    fun release() {
        handler.removeCallbacks(fadeStep)
        try {
            player?.release()
        } catch (e: RuntimeException) {
            // ignore
        }
        player = null
    }

    private companion object {
        const val TARGET_VOLUME = 0.5f
    }
}
