// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AuraDarkColorScheme = darkColorScheme(
    primary        = AuraViolet,
    secondary      = AuraAmber,
    background     = AuraBackground,
    surface        = AuraSurface,
    onPrimary      = AuraOnPrimary,
    onSecondary    = AuraOnPrimary,
    onBackground   = AuraOnPrimary,
    onSurface      = AuraOnPrimary,
)

@Composable
fun AuraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuraDarkColorScheme,
        typography  = AuraTypography,
        shapes      = AuraShapes,
        content     = content
    )
}