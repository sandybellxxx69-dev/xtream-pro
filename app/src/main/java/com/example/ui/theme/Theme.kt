package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme =
  darkColorScheme(
    primary = PrimaryRed,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = BottomNavDark,
    onPrimary = Color.White,
    onBackground = WhiteSoft,
    onSurface = WhiteSoft,
    onSurfaceVariant = TextGray
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = AppColorScheme, typography = Typography, content = content)
}
