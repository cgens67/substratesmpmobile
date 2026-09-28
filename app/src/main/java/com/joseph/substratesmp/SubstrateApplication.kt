package com.joseph.substratesmp

import android.app.Application
import android.os.Build
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.firebase.FirebaseApp

class SubstrateApplication : Application(), ImageLoaderFactory {
  companion object {
    var instance: SubstrateApplication? = null
      private set
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
