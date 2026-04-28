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

import androidx.compose.ui.graphics.Color

// ── Base surfaces ──────────────────────────────────────────────────────────

/** Deepest background layer — near-black with a faint blue tint. */
val AuraBlack   = Color(0xFF0A0A0F)

/** Primary surface colour used for screen backgrounds. */
val AuraSurface = Color(0xFF12121A)

/** Elevated card / sheet surface — one step above [AuraSurface]. */
val AuraCard    = Color(0xFF1C1C28)

// ── Glass material tokens ──────────────────────────────────────────────────

/** White at 8 % alpha — glass fill tint. */
val GlassWhite  = Color(0x14FFFFFF)   // 0x14 ≈ 8 %

/** White at 12 % alpha — glass border highlight. */
val GlassBorder = Color(0x1FFFFFFF)   // 0x1F ≈ 12 %

// ── Accent — Electric Violet ───────────────────────────────────────────────

/** Primary accent — electric violet. */
val AuraViolet     = Color(0xFF8B5CF6)

/** Violet at 40 % alpha — used for glow layers and tinted backgrounds. */
val AuraVioletGlow = Color(0x668B5CF6) // 0x66 ≈ 40 %

// ── Accent — Amber ────────────────────────────────────────────────────────

/** Secondary accent — warm amber for highlights and active states. */
val AuraAmber     = Color(0xFFF59E0B)

/** Amber at 40 % alpha — used for glow layers and tinted backgrounds. */
val AuraAmberGlow = Color(0x66F59E0B) // 0x66 ≈ 40 %

// ── Typography ────────────────────────────────────────────────────────────

/** White at 95 % alpha — primary readable text. */
val TextPrimary   = Color(0xF2FFFFFF)  // 0xF2 ≈ 95 %

/** White at 60 % alpha — secondary / metadata text. */
val TextSecondary = Color(0x99FFFFFF)  // 0x99 ≈ 60 %

// ── Supplementary semantic colours ────────────────────────────────────────

/** Error / destructive action colour. */
val AuraError     = Color(0xFFEF4444)

/** Success / confirmation colour. */
val AuraSuccess   = Color(0xFF22C55E)

/** Scrim overlay for modal surfaces. */
val AuraScrim     = Color(0xCC0A0A0F)  // 80 % opaque black-tinted surface