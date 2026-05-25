package com.example.kassaku.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Premium Haptic Feedback Manager
 * Handles subtle vibrations for success, error, and interaction events.
 */
class HapticManager(private val context: Context) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * Subtle tick for light interactions (like toggles)
     */
    fun lightTick() {
        vibrate(VibrationEffect.Composition.PRIMITIVE_TICK, 0.3f)
    }

    /**
     * Heavier click for main buttons
     */
    fun heavyClick() {
        vibrate(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f)
    }

    /**
     * Double pulse for successful actions
     */
    fun success() {
        if (!vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                Thread.sleep(100)
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 50, 50, 50), -1)
            }
        } catch (_: Exception) { }
    }

    /**
     * Staccato vibration for errors
     */
    fun error() {
        if (!vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
            }
        } catch (_: Exception) { }
    }

    private fun vibrate(primitiveId: Int, scale: Float) {
        if (!vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibrator.vibrate(
                    VibrationEffect.startComposition()
                        .addPrimitive(primitiveId, scale)
                        .compose()
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (_: Exception) {
            // Ignore on devices/emulators without vibration support
        }
    }
}
