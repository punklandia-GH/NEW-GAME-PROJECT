package com.blockcrush.puzzle

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Short vibrations for placing pieces and clearing lines. Respects the vibration setting. */
class Haptics(context: Context, private val prefs: Prefs) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /** Picking up a piece. */
    fun tap() = vibrate(10L, 70)

    /** Dropping a piece on the board. */
    fun place() = vibrate(18L, 140)

    /** One or more lines cleared. */
    fun clear(lines: Int) = vibrate((28L + 8L * lines.coerceAtMost(4)), 255)

    /** The whole board was cleared. */
    fun celebrate() = pattern(longArrayOf(0, 40, 50, 40, 50, 90))

    fun gameOver() = pattern(longArrayOf(0, 60, 90, 140))

    private fun vibrate(millis: Long, amplitude: Int) {
        if (!prefs.vibrationOn) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = if (v.hasAmplitudeControl()) amplitude else VibrationEffect.DEFAULT_AMPLITUDE
                v.vibrate(VibrationEffect.createOneShot(millis, amp))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(millis)
            }
        } catch (e: RuntimeException) {
            // Some devices throw for unsupported effects; vibration is never worth a crash.
        }
    }

    private fun pattern(timings: LongArray) {
        if (!prefs.vibrationOn) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(timings, -1)
            }
        } catch (e: RuntimeException) {
            // Ignore: see above.
        }
    }
}
