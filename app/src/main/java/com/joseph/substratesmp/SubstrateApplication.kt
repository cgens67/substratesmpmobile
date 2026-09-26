package com.joseph.substratesmp

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class SubstrateApplication : Application() {
  companion object {
    var instance: SubstrateApplication? = null
      private set
  }

  override fun onCreate() {
    super.onCreate()
    instance = this

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
