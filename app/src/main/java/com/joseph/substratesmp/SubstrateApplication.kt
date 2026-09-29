package com.joseph.substratesmp

import android.app.Application
import android.os.Build
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubstrateApplication : Application(), ImageLoaderFactory {
  companion object {
    var instance: SubstrateApplication? = null
      private set

    const val ONESIGNAL_APP_ID = "c304cfb2-a08d-45b0-b1c4-a90b96be3e05"
  }

  override fun newImageLoader(): ImageLoader {
    return ImageLoader.Builder(this)
      .components {
        if (Build.VERSION.SDK_INT >= 28) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
      }
      .build()
  }

  override fun onCreate() {
    super.onCreate()
    instance = this

    // 1. Initialize OneSignal (Handles background wake-up and push notifications)
    try {
      OneSignal.Debug.logLevel = LogLevel.WARN
      OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

      // Request push notification permissions automatically
      CoroutineScope(Dispatchers.IO).launch {
        OneSignal.Notifications.requestPermission(false)
      }
      Log.i("SubstrateApplication", "OneSignal initialized with App ID: $ONESIGNAL_APP_ID")
    } catch (e: Exception) {
      Log.e("SubstrateApplication", "OneSignal initialization error: ${e.message}")
    }

    // 2. Initialize Firebase
    try {
      if (FirebaseApp.getApps(this).isEmpty()) {
        FirebaseApp.initializeApp(this)
        Log.i("SubstrateApplication", "Firebase initialized in Application.onCreate")
      }
    } catch (e: Exception) {
      Log.w("SubstrateApplication", "Firebase initialization in Application: ${e.message}")
    }
  }
}
