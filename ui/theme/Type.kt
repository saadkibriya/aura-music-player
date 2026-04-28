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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.kibriya.aura.R

// ── Google Fonts provider ──────────────────────────────────────────────────

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

private val interGoogleFont = GoogleFont(name = "Inter")

// ── Inter font family ──────────────────────────────────────────────────────

/**
 * Inter downloadable font family via Google Fonts at runtime.
 *
 * Falls back to the system sans-serif if the device has no GMS or no
 * network at first launch; subsequent runs use the cached font.
 */
val InterFontFamily = FontFamily(
    Font(googleFont = interGoogleFont, fontProvider = googleFontProvider,
        weight = FontWeight.Light),
    Font(googleFont = interGoogleFont, fontProvider = googleFontProvider,
        weight = FontWeight.Normal),
    Font(googleFont = interGoogleFont, fontProvider = googleFontProvider,
        weight = FontWeight.Medium),
    Font(googleFont = interGoogleFont, fontProvider = googleFontProvider,
        weight = FontWeight.SemiBold),
    Font(googleFont = interGoogleFont, fontProvider = googleFontProvider,
        weight = FontWeight.Bold),
    Font(googleFont = interGoogleFont, fontProvider = googleFontProvider,
        weight = FontWeight.ExtraBold),
)

// ── Aura Typography scale ──────────────────────────────────────────────────

/**
 * Full Material 3 typography scale using [InterFontFamily].
 *
 * Scale rationale:
 *  - Display  → hero text, album titles, splash
 *  - Title    → section headers, playlist names, artist names
 *  - Body     → track metadata, descriptions, settings text
 *  - Label    → pills, tags, timestamps, mini-player metadata
 */
val AuraTypography = Typography(

    // ── Display ─────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.Bold,
        fontSize    = 57.sp,
        lineHeight  = 64.sp,
        letterSpacing = (-0.25).sp,
        color       = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 45.sp,
        lineHeight  = 52.sp,
        letterSpacing = 0.sp,
        color       = TextPrimary
    ),
    displaySmall = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 36.sp,
        lineHeight  = 44.sp,
        letterSpacing = 0.sp,
        color       = TextPrimary
    ),

    // ── Title ────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.SemiBold,
        fontSize    = 22.sp,
        lineHeight  = 28.sp,
        letterSpacing = 0.sp,
        color       = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 16.sp,
        lineHeight  = 24.sp,
        letterSpacing = 0.15.sp,
        color       = TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.1.sp,
        color       = TextPrimary
    ),

    // ── Body ─────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 16.sp,
        lineHeight  = 24.sp,
        letterSpacing = 0.5.sp,
        color       = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.Normal,
        fontSize    = 14.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.25.sp,
        color       = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.Light,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.4.sp,
        color       = TextSecondary
    ),

    // ── Label ────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 20.sp,
        letterSpacing = 0.1.sp,
        color       = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.5.sp,
        color       = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily  = InterFontFamily,
        fontWeight  = FontWeight.Medium,
        fontSize    = 11.sp,
        lineHeight  = 16.sp,
        letterSpacing = 0.5.sp,
        color       = TextSecondary
    ),
)