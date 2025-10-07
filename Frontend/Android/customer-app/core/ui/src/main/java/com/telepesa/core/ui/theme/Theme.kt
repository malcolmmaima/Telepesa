/*
 * Copyright (c) 2025 Telepesa. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.telepesa.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Telepesa app theme configuration.
 * Implements Material Design 3 with custom brand colors.
 */

private val LightColorScheme = lightColorScheme(
    primary = TelepesaPrimary,
    onPrimary = TelepesaOnPrimary,
    primaryContainer = TelepesaPrimaryVariant,
    onPrimaryContainer = TelepesaOnPrimary,

    secondary = TelepesaSecondary,
    onSecondary = TelepesaOnSecondary,
    secondaryContainer = TelepesaSecondary.copy(alpha = 0.1f),
    onSecondaryContainer = TelepesaPrimary,

    tertiary = TelepesaInfo,
    onTertiary = TelepesaOnPrimary,
    tertiaryContainer = TelepesaInfo.copy(alpha = 0.1f),
    onTertiaryContainer = TelepesaInfo,

    error = TelepesaError,
    onError = TelepesaOnPrimary,
    errorContainer = TelepesaError.copy(alpha = 0.1f),
    onErrorContainer = TelepesaError,

    background = TelepesaBackground,
    onBackground = TelepesaOnBackground,

    surface = TelepesaSurface,
    onSurface = TelepesaOnSurface,
    surfaceVariant = TelepesaSurfaceVariant,
    onSurfaceVariant = TelepesaOnSurface.copy(alpha = 0.7f),

    outline = TelepesaBorder,
    outlineVariant = TelepesaBorder.copy(alpha = 0.5f),

    scrim = TelepesaOnSurface.copy(alpha = 0.32f),
)

private val DarkColorScheme = darkColorScheme(
    primary = TelepesaDarkPrimary,
    onPrimary = TelepesaOnPrimary,
    primaryContainer = TelepesaDarkPrimary.copy(alpha = 0.2f),
    onPrimaryContainer = TelepesaDarkPrimary,

    secondary = TelepesaSecondary,
    onSecondary = TelepesaOnSecondary,
    secondaryContainer = TelepesaSecondary.copy(alpha = 0.2f),
    onSecondaryContainer = TelepesaSecondary,

    tertiary = TelepesaInfo,
    onTertiary = TelepesaOnPrimary,
    tertiaryContainer = TelepesaInfo.copy(alpha = 0.2f),
    onTertiaryContainer = TelepesaInfo,

    error = TelepesaError,
    onError = TelepesaOnPrimary,
    errorContainer = TelepesaError.copy(alpha = 0.2f),
    onErrorContainer = TelepesaError,

    background = TelepesaDarkBackground,
    onBackground = TelepesaDarkOnBackground,

    surface = TelepesaDarkSurface,
    onSurface = TelepesaDarkOnSurface,
    surfaceVariant = TelepesaDarkSurface.copy(alpha = 0.8f),
    onSurfaceVariant = TelepesaDarkOnSurface.copy(alpha = 0.7f),

    outline = TelepesaBorder.copy(alpha = 0.5f),
    outlineVariant = TelepesaBorder.copy(alpha = 0.3f),

    scrim = TelepesaDarkOnSurface.copy(alpha = 0.32f),
)

@Composable
fun telepesaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TelepesaTypography,
        content = content,
    )
}
