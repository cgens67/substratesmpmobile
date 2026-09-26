package com.joseph.substratesmp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = CyanPrimary,
  onPrimary = CyanOnPrimary,
  primaryContainer = CyanPrimaryContainer,
  onPrimaryContainer = CyanOnPrimaryContainer,
  secondary = EmeraldSecondary,
  onSecondary = EmeraldOnSecondary,
  secondaryContainer = EmeraldSecondaryContainer,
  onSecondaryContainer = EmeraldOnSecondaryContainer,
  tertiary = PortalPurpleTertiary,
  onTertiary = PortalPurpleOnTertiary,
  tertiaryContainer = PortalPurpleTertiaryContainer,
  onTertiaryContainer = PortalPurpleOnTertiaryContainer,
  background = BedrockDark,
  onBackground = BedrockOnSurface,
  surface = BedrockSurface,
  onSurface = BedrockOnSurface,
  surfaceVariant = BedrockSurfaceContainer,
  onSurfaceVariant = BedrockOnSurfaceVariant,
  surfaceContainerLowest = BedrockSurfaceContainerLowest,
  surfaceContainerLow = BedrockSurfaceContainerLow,
  surfaceContainer = BedrockSurfaceContainer,
  surfaceContainerHigh = BedrockSurfaceContainerHigh,
  surfaceContainerHighest = BedrockSurfaceContainerHighest,
  surfaceDim = BedrockSurfaceDim,
  surfaceBright = BedrockSurfaceBright,
  outline = BedrockOutline,
  outlineVariant = BedrockOutlineVariant,
)

private val LightColorScheme = lightColorScheme(
  primary = LightPrimary,
  onPrimary = LightOnPrimary,
  primaryContainer = LightPrimaryContainer,
  onPrimaryContainer = LightOnPrimaryContainer,
  secondary = LightSecondary,
  onSecondary = LightOnSecondary,
  secondaryContainer = LightSecondaryContainer,
  onSecondaryContainer = LightOnSecondaryContainer,
  tertiary = LightTertiary,
  onTertiary = LightOnTertiary,
  tertiaryContainer = LightTertiaryContainer,
  onTertiaryContainer = LightOnTertiaryContainer,
  background = LightBackground,
  onBackground = LightOnBackground,
  surface = LightSurface,
  onSurface = LightOnSurface,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightOnSurfaceVariant,
  outline = LightOutline,
)

val LocalSubstrateColors = staticCompositionLocalOf { SubstrateCustomColors() }

object SubstrateTheme {
  val customColors: SubstrateCustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSubstrateColors.current

  val colorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme
}

@Composable
fun SubstrateSMPTheme(
  darkTheme: Boolean = true, // Default to immersive dark mode for Discord/gaming community
  dynamicColor: Boolean = false, // Preserve branded Substrate Minecraft styling
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  val customColors = SubstrateCustomColors()

  CompositionLocalProvider(
    LocalSubstrateColors provides customColors
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = SubstrateTypography,
      shapes = SubstrateShapes,
      content = content
    )
  }
}
