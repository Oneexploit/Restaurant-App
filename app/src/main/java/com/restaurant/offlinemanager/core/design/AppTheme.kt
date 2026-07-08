package com.restaurant.offlinemanager.core.design

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = BackgroundStart,
    secondary = AppCyan,
    onSecondary = TextPrimary,
    background = BackgroundStart,
    onBackground = TextPrimary,
    surface = SurfaceGlass,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGlassStrong,
    onSurfaceVariant = TextSecondary,
    error = AppRed,
    onError = TextPrimary
)

val LocalMotionEnabled = staticCompositionLocalOf { true }

@Composable
fun RestaurantOfflineTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = DarkColors,
            typography = AppTypography,
            content = content
        )
    }
}

fun Modifier.appBackground(): Modifier =
    background(
        Brush.verticalGradient(
            listOf(
                BackgroundStart,
                BackgroundMid,
                BackgroundEnd
            )
        )
    )

fun premiumBackgroundBrush(): Brush =
    Brush.verticalGradient(
        listOf(
            BackgroundStart,
            BackgroundMid,
            BackgroundEnd
        )
    )
