package com.joseph.substratesmp.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object CallRingtoneHelper {
  private const val TAG = "CallRingtoneHelper"
  private var mediaPlayer: MediaPlayer? = null
  private var vibrator: Vibrator? = null

  fun startRinging(context: Context) {
    stopRinging(context)

    try {
      val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

      mediaPlayer = MediaPlayer().apply {
        setDataSource(context, ringtoneUri)
        setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        isLooping = true
        prepare()
        start()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to start call ringtone: ${e.message}")
    }

    try {
      vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      }

      val pattern = longArrayOf(0, 1000, 1000, 1000, 1000)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 1))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(pattern, 1)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to start vibrator: ${e.message}")
    }
  }

  fun stopRinging(context: Context) {
    try {
      mediaPlayer?.stop()
      mediaPlayer?.release()
      mediaPlayer = null
    } catch (_: Exception) {}

    try {
      vibrator?.cancel()
      vibrator = null
    } catch (_: Exception) {}
  }
}
