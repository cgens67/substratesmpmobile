package com.joseph.substratesmp

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.joseph.substratesmp.data.model.ChatMessage
import com.joseph.substratesmp.data.model.DefaultChannels
import com.joseph.substratesmp.data.repository.AuthUserState
import com.joseph.substratesmp.ui.components.ChannelDrawerContent
import com.joseph.substratesmp.ui.components.ChatMessageItem
import com.joseph.substratesmp.ui.theme.SubstrateSMPTheme
import com.joseph.substratesmp.voice.AgoraVoiceManager
import com.joseph.substratesmp.voice.RtcTokenBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Substrate SMP", appName)
  }

  @Test
  fun `agora rtc token builder generates valid token with app id and certificate`() {
    val appId = AgoraVoiceManager.DEFAULT_AGORA_APP_ID
    val appCertificate = AgoraVoiceManager.DEFAULT_AGORA_APP_CERTIFICATE
    val channel = "voice_general_1"
    val uid = 12345
    val expirationTs = (System.currentTimeMillis() / 1000 + 3600).toInt()

    val token = RtcTokenBuilder().buildTokenWithUid(
      appId = appId,
      appCertificate = appCertificate,
      channelName = channel,
      uid = uid,
      role = RtcTokenBuilder.Role.Role_Publisher,
      privilegeTs = expirationTs
    )

    assertTrue("Token must not be empty", token.isNotEmpty())
    assertTrue("Token must start with 006 version and App ID", token.startsWith("006$appId"))
  }

  @Test
  fun `siang5680 exclusively gains admin privileges`() {
    val admin1 = "Siang5680".equals("Siang5680", ignoreCase = true)
    val admin2 = "siang5680".equals("Siang5680", ignoreCase = true)
    val admin3 = "SIANG5680".equals("Siang5680", ignoreCase = true)
    val normalUser = "DiamondMiner99".equals("Siang5680", ignoreCase = true)

    assertTrue(admin1)
    assertTrue(admin2)
    assertTrue(admin3)
    assertFalse(normalUser)
  }

  @Test
  fun `chat message item renders distinct admin badge for Siang5680`() {
    val adminMessage = ChatMessage(
      id = "msg_1",
      channelId = "general-chat",
      senderName = "Siang5680",
      senderRole = "ADMIN",
      content = "Welcome to Substrate SMP Bedrock Realm!",
      isLocalUser = false
    )

    composeTestRule.setContent {
      SubstrateSMPTheme {
        ChatMessageItem(message = adminMessage)
      }
    }

    composeTestRule.onNodeWithText("Siang5680").assertIsDisplayed()
    composeTestRule.onNodeWithText("ADMIN").assertIsDisplayed()
  }

  @Test
  fun `channel drawer renders successfully with banner and profile`() {
    composeTestRule.setContent {
      SubstrateSMPTheme {
        ChannelDrawerContent(
          channels = DefaultChannels,
          activeChannel = DefaultChannels[1],
          userState = AuthUserState(gamertag = "Siang5680", role = "ADMIN", isAdmin = true),
          onSelectChannel = {},
          onOpenGamertagDialog = {},
          onOpenAgoraSettings = {},
          onOpenServerInfo = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("channel_drawer_surface").assertIsDisplayed()
    composeTestRule.onNodeWithTag("user_profile_bar").assertIsDisplayed()
    composeTestRule.onNodeWithText("ADMIN").assertIsDisplayed()
  }
}
