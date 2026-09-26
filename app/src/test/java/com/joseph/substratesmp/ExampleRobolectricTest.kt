package com.joseph.substratesmp

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.core.app.ApplicationProvider
import com.joseph.substratesmp.data.model.Channel
import com.joseph.substratesmp.data.model.ChannelType
import com.joseph.substratesmp.data.model.SeedChannels
import com.joseph.substratesmp.data.repository.AuthUserState
import com.joseph.substratesmp.ui.components.ChannelDrawerContent
import com.joseph.substratesmp.ui.theme.SubstrateSMPTheme
import org.junit.Assert.assertEquals
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
  fun `channel drawer renders successfully with banner and channels`() {
    composeTestRule.setContent {
      SubstrateSMPTheme {
        ChannelDrawerContent(
          channels = SeedChannels,
          activeChannel = SeedChannels[1],
          userState = AuthUserState(gamertag = "TestMiner", role = "BUILDER"),
          onSelectChannel = {},
          onOpenGamertagDialog = {},
          onOpenAgoraSettings = {},
          onOpenServerInfo = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("channel_drawer_surface").assertIsDisplayed()
    composeTestRule.onNodeWithTag("user_profile_bar").assertIsDisplayed()
  }
}
