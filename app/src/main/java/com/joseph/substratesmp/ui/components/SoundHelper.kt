package com.joseph.substratesmp.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log

object SoundHelper {
  private val TAG = "SoundHelper"

  /**
   * Plays the incoming chat sound.
   * Place your MP3 file at:
   * app/src/main/res/raw/message_received.mp3
   *
   * If not found, it smoothly falls back to the system notification ringtone.
   */
  fun playMessageSound(context: Context) {
    try {
      val resId = context.resources.getIdentifier("message_received", "raw", context.packageName)
      if (resId != 0) {
        val player = MediaPlayer.create(context, resId)
        player?.setOnCompletionListener { it.release() }
        player?.start()
      } else {
        val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, defaultUri)
        ringtone?.play()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Audio playback notice: ${e.message}")
    }
  }
}
