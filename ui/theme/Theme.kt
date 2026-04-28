/*
 * MIT License
 *
 * Copyright (c) 2025 Md Golam Kibriya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.kibriya.aura.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Aura dark colour scheme ────────────────────────────────────────────────

/**
 * Forced dark colour scheme — Aura never respects a light system theme.
 *
 * Mapping rationale:
 *  primary          → [AuraViolet]    (main interactive accent)
 *  secondary        → [AuraAmber]     (secondary / warm accent)
 *  background       → [AuraBlack]     (deepest background)
 *  surface          → [AuraSurface]   (screen-level surface)
 *  surfaceVariant   → [AuraCard]      (elevated card surface)
 *  onPrimary        → black           (text / icon on violet fills)
 *  onSecondary      → black           (text / icon on amber fills)
 *  onBackground     → [TextPrimary]   (content on background)
 *  onSurface        → [TextPrimary]   (content on surface)
 *  onSurfaceVariant → [TextSecondary] (subdued content on cards)
 *  outline          → [GlassBorder]   (border / divider)
 *  scrim            → [AuraScrim]     (modal overlay)
 *  error            → [AuraError]
 */
private val AuraDarkColorScheme = darkColorScheme(
    primary              = AuraViolet,
    onPrimary            = Color.Black,
    primaryContainer     = AuraVioletGlow,
    onPrimaryContainer   = AuraViolet,

    secondary            = AuraAmber,
    onSecondary          = Color.Black,
    secondaryContainer   = AuraAmberGlow,
    onSecondaryContainer = AuraAmber,

    tertiary             = AuraAmber,
    onTertiary           = Color.Black,

    background           = AuraBlack,
    onBackground         = TextPrimary,

    surface              = AuraSurface,
    onSurface            = TextPrimary,
    surfaceVariant       = AuraCard,
    onSurfaceVariant     = TextSecondary,
    surfaceTint          = AuraViolet,

    inverseSurface       = TextPrimary,
    inverseOnSurface     = AuraBlack,
    inversePrimary       = AuraViolet,

    outline              = GlassBorder,
    outlineVariant       = GlassWhite,

    scrim                = AuraScrim,
    error                = AuraError,
    onError              = Color.White,
    errorContainer       = AuraError.copy(alpha = 0.2f),
    onErrorContainer     = AuraError,
)

// ── AuraTheme composable ───────────────────────────────────────────────────

/**
 * Root theme composable.
 *
 * - Always forces the dark colour scheme regardless of system setting.
 * - Applies transparent status bar and navigation bar via [SideEffect]
 *   so the edge-to-edge canvas bleeds to the physical display edges.
 * - Dark icon mode is explicitly disabled (light icons on dark surface).
 *
 * @param content  Composable content rendered within this theme scope.
 */
@Composable
fun AuraTheme(content: @Composable () -> Unit) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Fully transparent system bars — content draws edge-to-edge
            window.statusBarColor     = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            // Ensure light icons (dark theme — no dark icons needed)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AuraDarkColorScheme,
        typography  = AuraTypography,
        shapes      = AuraShapes,
        content     = content,
    )
}