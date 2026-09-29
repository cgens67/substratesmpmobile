package com.joseph.substratesmp.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.joseph.substratesmp.MainActivity

object NotificationHelper {
  private const val CHAT_CHANNEL_ID = "substrate_chat_channel"
  private const val CHAT_CHANNEL_NAME = "Substrate Messages"

  const val CALLS_CHANNEL_ID = "substrate_calls_channel"
  private const val CALLS_CHANNEL_NAME = "Substrate Incoming Calls"

  fun init(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      // 1. Chat Message Channel
      val chatChannel = NotificationChannel(
        CHAT_CHANNEL_ID,
        CHAT_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Notifications for incoming messages and direct messages"
        enableLights(true)
        enableVibration(true)
      }
      notificationManager.createNotificationChannel(chatChannel)

      // 2. High Priority Call Channel with Ringtone Attributes
      val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
      val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

      val callChannel = NotificationChannel(
        CALLS_CHANNEL_ID,
        CALLS_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Heads-up incoming call notifications"
        setSound(ringtoneUri, audioAttributes)
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 1000, 1000, 1000, 1000)
        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
      }
      notificationManager.createNotificationChannel(callChannel)
    }
  }

  fun showIncomingCallNotification(
    context: Context,
    callId: String,
    caller: String,
    isVideo: Boolean
  ) {
    init(context)

    // Full screen / Tap Intent to open call screen
    val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
      action = "ACTION_ANSWER_CALL"
      putExtra("callId", callId)
      putExtra("caller", caller)
      putExtra("isVideo", isVideo)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val fullScreenPendingIntent = PendingIntent.getActivity(
      context,
      callId.hashCode(),
      fullScreenIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Answer action intent
    val answerIntent = Intent(context, MainActivity::class.java).apply {
      action = "ACTION_ANSWER_CALL"
      putExtra("callId", callId)
      putExtra("caller", caller)
      putExtra("isVideo", isVideo)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val answerPendingIntent = PendingIntent.getActivity(
      context,
      callId.hashCode() + 1,
      answerIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Decline broadcast intent
    val declineIntent = Intent(context, CallActionReceiver::class.java).apply {
      action = "ACTION_DECLINE_CALL"
      putExtra("callId", callId)
    }
    val declinePendingIntent = PendingIntent.getBroadcast(
      context,
      callId.hashCode() + 2,
      declineIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val callerPerson = Person.Builder()
      .setName(caller)
      .setImportant(true)
      .build()

    val appIcon = context.applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.sym_call_incoming

    val callStyle = NotificationCompat.CallStyle.forIncomingCall(
      callerPerson,
      declinePendingIntent,
      answerPendingIntent
    ).setIsVideo(isVideo)

    val notification = NotificationCompat.Builder(context, CALLS_CHANNEL_ID)
      .setSmallIcon(appIcon)
      .setContentTitle("Incoming ${if (isVideo) "Video" else "Voice"} Call")
      .setContentText("$caller is calling you...")
      .setStyle(callStyle)
      .addPerson(callerPerson)
      .setCategory(NotificationCompat.CATEGORY_CALL)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setOngoing(true)
      .setAutoCancel(false)
      .setFullScreenIntent(fullScreenPendingIntent, true)
      .setContentIntent(fullScreenPendingIntent)
      .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", declinePendingIntent)
      .addAction(android.R.drawable.ic_menu_call, "Answer", answerPendingIntent)
      .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(callId.hashCode(), notification)
  }

  fun dismissCallNotification(context: Context, notificationId: Int? = null) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (notificationId != null) {
      notificationManager.cancel(notificationId)
    } else {
      notificationManager.cancelAll()
    }
  }

  fun showMessageNotification(
    context: Context,
    notificationId: Int,
    title: String,
    content: String
  ) {
    init(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val appIcon = context.applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.stat_notify_chat

    val notification = NotificationCompat.Builder(context, CHAT_CHANNEL_ID)
      .setSmallIcon(appIcon)
      .setContentTitle(title)
      .setContentText(content)
      .setAutoCancel(true)
      .setSound(soundUri)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setContentIntent(pendingIntent)
      .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(notificationId, notification)
  }
}
