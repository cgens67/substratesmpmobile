package com.joseph.substratesmp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joseph.substratesmp.ui.SubstrateApp
import com.joseph.substratesmp.ui.SubstrateViewModel
import com.joseph.substratesmp.ui.components.CallRingtoneHelper
import com.joseph.substratesmp.ui.components.NotificationHelper
import com.joseph.substratesmp.ui.theme.SubstrateSMPTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
  private val viewModel: SubstrateViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    viewModel.voiceManager.initAgoraEngine()
    handleCallIntent(intent)

    setContent {
      val settings by viewModel.appSettings.collectAsStateWithLifecycle()

      // Resolve user's chosen language into actual Android Locale
      val targetLocale = remember(settings.language) {
        when (settings.language.lowercase()) {
          "malay" -> Locale("ms")
          "chinese" -> Locale.SIMPLIFIED_CHINESE
          else -> Locale.ENGLISH
        }
      }

      val context = LocalContext.current
      val configuration = remember(targetLocale) {
        Locale.setDefault(targetLocale)
        val config = Configuration(context.resources.configuration).apply {
          setLocale(targetLocale)
        }
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        @Suppress("DEPRECATION")
        context.applicationContext.resources.updateConfiguration(config, context.applicationContext.resources.displayMetrics)
        config
      }

      // Safe locale provider that does NOT overwrite LocalContext (which broke ActivityResultLauncher)
      CompositionLocalProvider(
        LocalConfiguration provides configuration
      ) {
        key(settings.language) {
          SubstrateSMPTheme(darkTheme = settings.isNightMode) {
            Surface(
              modifier = Modifier.fillMaxSize(),
              color = MaterialTheme.colorScheme.background
            ) {
              SubstrateApp(viewModel = viewModel)
            }
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleCallIntent(intent)
  }

  private fun handleCallIntent(intent: Intent?) {
    if (intent == null) return
    if (intent.action == "ACTION_ANSWER_CALL") {
      val callId = intent.getStringExtra("callId") ?: return
      CallRingtoneHelper.stopRinging(this)
      NotificationHelper.dismissCallNotification(this, callId.hashCode())
      viewModel.answerIncomingCall()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    CallRingtoneHelper.stopRinging(this)
  }
}
