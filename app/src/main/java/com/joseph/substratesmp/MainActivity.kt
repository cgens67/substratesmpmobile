package com.joseph.substratesmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.joseph.substratesmp.ui.SubstrateApp
import com.joseph.substratesmp.ui.SubstrateViewModel
import com.joseph.substratesmp.ui.theme.SubstrateSMPTheme

class MainActivity : ComponentActivity() {
  private val viewModel: SubstrateViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    viewModel.voiceManager.initAgoraEngine()
    setContent {
      SubstrateSMPTheme {
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
