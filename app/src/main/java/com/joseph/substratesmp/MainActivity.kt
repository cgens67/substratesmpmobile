package com.joseph.substratesmp

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joseph.substratesmp.ui.SubstrateApp
import com.joseph.substratesmp.ui.SubstrateViewModel
import com.joseph.substratesmp.ui.theme.SubstrateSMPTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
  private val viewModel: SubstrateViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    viewModel.voiceManager.initAgoraEngine()
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

      LaunchedEffect(targetLocale) {
        Locale.setDefault(targetLocale)
      }

      val baseContext = LocalContext.current
      val localizedContext = remember(baseContext, targetLocale) {
        val config = Configuration(baseContext.resources.configuration).apply {
          setLocale(targetLocale)
        }
        baseContext.createConfigurationContext(config)
      }

      val localizedConfiguration = remember(localizedContext) {
        localizedContext.resources.configuration
      }

      // Provides the active language configuration to stringResource across all Composables
      CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration
      ) {
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
