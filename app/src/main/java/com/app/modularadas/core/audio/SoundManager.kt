package com.app.modularadas.core.audio

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages audio alerts for ADAS warnings.
 * Uses system notification sounds for reliable beeping.
 */
class SoundManager(private val context: Context) {
    companion object {
        private const val TAG = "SoundManager"
    }

    private var beepJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * Play a slow beep for caution/orange warnings.
     * Uses default notification sound.
     */
    fun playSlowBeep() {
        Log.d(TAG, "[playSlowBeep] Starting slow beep")
        beepJob?.cancel()
        beepJob = scope.launch {
            try {
                Log.d(TAG, "[playSlowBeep] Getting notification sound URI")
                val notificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                Log.d(TAG, "[playSlowBeep] Playing notification sound: $notificationUri")
                val ringtone = RingtoneManager.getRingtone(context, notificationUri)
                ringtone.play()
                Log.d(TAG, "[playSlowBeep] Slow beep started")
                delay(300)
                ringtone.stop()
                Log.d(TAG, "[playSlowBeep] Slow beep complete")
            } catch (e: Exception) {
                Log.e(TAG, "[playSlowBeep] Error playing slow beep", e)
            }
        }
    }

    /**
     * Play a fast beep for critical/red warnings.
     * Uses alarm sound.
     */
    fun playFastBeep() {
        Log.d(TAG, "[playFastBeep] Starting fast beep")
        beepJob?.cancel()
        beepJob = scope.launch {
            try {
                Log.d(TAG, "[playFastBeep] Getting alarm sound URI")
                val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                Log.d(TAG, "[playFastBeep] Playing alarm sound: $alarmUri")
                val ringtone = RingtoneManager.getRingtone(context, alarmUri)
                ringtone.play()
                Log.d(TAG, "[playFastBeep] Fast beep started")
                delay(200)
                ringtone.stop()
                Log.d(TAG, "[playFastBeep] Fast beep complete")
            } catch (e: Exception) {
                Log.e(TAG, "[playFastBeep] Error playing fast beep", e)
            }
        }
    }

    fun release() {
        beepJob?.cancel()
        beepJob = null
    }
}
