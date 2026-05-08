package com.app.modularadas.core.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.*
import kotlin.math.max

// Coroutine-based dynamic beep frequency generator
class AudioPlayer(context: Context) {
    private val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
    private var alertJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Starts or updates the dynamic alerting loop.
     * @param distance Distance in meters. Frequency scales inversely.
     */
    fun updateAlertFrequency(distance: Float) {
        val delayMs = calculateDelay(distance)
        
        if (delayMs == -1L) {
            stopAlerts()
            return
        }

        // Restart loop with new frequency if active
        alertJob?.cancel()
        alertJob = scope.launch {
            while (isActive) {
                toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 150)
                delay(delayMs)
            }
        }
    }

    fun stopAlerts() {
        alertJob?.cancel()
        alertJob = null
    }

    private fun calculateDelay(distance: Float): Long {
        return when {
            distance <= com.app.modularadas.core.constants.AdasConfig.THRESHOLD_CRITICAL_METERS -> 200L // Fast beep
            distance <= com.app.modularadas.core.constants.AdasConfig.THRESHOLD_WARNING_METERS -> {
                // Scale from 200ms (at critical) to 1000ms (at warning edge)
                val range = com.app.modularadas.core.constants.AdasConfig.THRESHOLD_WARNING_METERS - com.app.modularadas.core.constants.AdasConfig.THRESHOLD_CRITICAL_METERS
                val progression = (distance - com.app.modularadas.core.constants.AdasConfig.THRESHOLD_CRITICAL_METERS) / range
                max(200L, (200L + (800 * progression)).toLong())
            }
            else -> -1L // Safe, no beep
        }
    }
}