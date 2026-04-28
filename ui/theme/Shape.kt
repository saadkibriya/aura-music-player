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

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── Named shape tokens ─────────────────────────────────────────────────────

/**
 * 50 % corner radius — renders as a perfect pill/stadium for
 * any aspect ratio.  Used for primary action buttons, tags, and the
 * floating navbar pill.
 */
val PillShape = RoundedCornerShape(percent = 50)

/**
 * 28 dp corner radius — iOS-style squircle approximation.
 * Used for album art thumbnails, large interactive tiles, and
 * prominent media cards.
 */
val SquircleShape = RoundedCornerShape(28.dp)

/**
 * 16 dp corner radius — standard content card.
 * Used for track list items, queue cards, and info panels.
 */
val CardShape = RoundedCornerShape(16.dp)

/**
 * Top-only 24 dp corners — bottom sheet and mini-player surface that
 * slides up from the bottom of the screen.
 */
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

/**
 * 12 dp corner — compact chip, small badge, and secondary tag.
 */
val ChipShape = RoundedCornerShape(12.dp)

/**
 * 8 dp corner — sub-card inset, image within a card, or input field.
 */
val InsetShape = RoundedCornerShape(8.dp)

// ── Material 3 Shapes mapping ──────────────────────────────────────────────

/**
 * [Shapes] instance wired into [AuraTheme].
 *
 * Material 3 shape scale:
 *  - extraSmall / small  → [ChipShape] / [InsetShape]
 *  - medium              → [CardShape]
 *  - large               → [SquircleShape]
 *  - extraLarge          → [BottomSheetShape]
 */
val AuraShapes = Shapes(
    extraSmall = ChipShape,
    small      = InsetShape,
    medium     = CardShape,
    large      = SquircleShape,
    extraLarge = BottomSheetShape,
)