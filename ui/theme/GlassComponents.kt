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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ══════════════════════════════════════════════════════════════════════════════
//  Modifier extensions
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Applies a frosted-glass surface treatment to any composable.
 *
 * Implementation layers (bottom → top):
 *  1. Diagonal linear-gradient fill at [alpha] opacity (white tint)
 *  2. [BlurEffect] via [graphicsLayer] — uses Android 12+ [RenderEffect]
 *     to diffuse the surface, softening whatever is rendered beneath
 *  3. [GlassBorder] 0.5 dp stroke for the characteristic glass edge glint
 *
 * The blur is applied at [blurRadius] * 0.35 so the composable's own text /
 * icon content remains legible while the surface still reads as diffused.
 *
 * @param blurRadius  Full logical blur radius in pixels (default 24f).
 * @param alpha       White fill opacity (default 0.08f = GlassWhite token).
 * @param shape       Clipping shape; defaults to [CardShape].
 */
fun Modifier.glassBackground(
    blurRadius : Float = 24f,
    alpha      : Float = 0.08f,
    shape      : Shape = CardShape,
): Modifier = this
    .graphicsLayer {
        // RenderEffect: BlurEffect wraps android.graphics.RenderEffect (API 31+)
        renderEffect = BlurEffect(
            radiusX       = blurRadius * 0.35f,
            radiusY       = blurRadius * 0.35f,
            edgeTreatment = TileMode.Clamp
        )
        clip = true
    }
    .background(
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color.White.copy(alpha = alpha + 0.06f),
                0.5f to Color.White.copy(alpha = alpha + 0.02f),
                1.0f to Color.White.copy(alpha = alpha)
            ),
            start = Offset(0f, 0f),
            end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        ),
        shape = shape
    )
    .border(width = 0.5.dp, color = GlassBorder, shape = shape)

// ──────────────────────────────────────────────────────────────────────────────

/**
 * Paints a soft coloured glow halo behind the composable.
 *
 * Rendered via [drawBehind] on a hardware-accelerated canvas using a
 * [Paint] with [android.graphics.BlurMaskFilter].  A companion
 * [graphicsLayer] sets [ambientShadowColor] / [spotShadowColor] so that
 * the Material elevation shadow also picks up the [color] tint, giving a
 * consistent neon-glow effect at any elevation.
 *
 * @param color   Glow colour (use [AuraVioletGlow] or [AuraAmberGlow]).
 * @param radius  Blur spread radius (default 16 dp).
 * @param alpha   Glow opacity (default 0.55f).
 */
fun Modifier.auraGlow(
    color  : Color,
    radius : Dp    = 16.dp,
    alpha  : Float = 0.55f,
): Modifier = this
    .graphicsLayer {
        // Tint Material elevation shadow with the glow colour
        ambientShadowColor = color
        spotShadowColor    = color
        shadowElevation    = radius.toPx() * 0.5f
    }
    .drawBehind {
        val radiusPx = radius.toPx()
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val fp    = paint.asFrameworkPaint()
            fp.isAntiAlias = true
            fp.color       = color.copy(alpha = alpha).toArgb()
            fp.maskFilter  = android.graphics.BlurMaskFilter(
                radiusPx,
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
            canvas.drawRoundRect(
                left    = -radiusPx * 0.25f,
                top     = -radiusPx * 0.25f,
                right   =  size.width  + radiusPx * 0.25f,
                bottom  =  size.height + radiusPx * 0.25f,
                radiusX =  radiusPx * 0.5f,
                radiusY =  radiusPx * 0.5f,
                paint   =  paint
            )
        }
    }

// ──────────────────────────────────────────────────────────────────────────────

/**
 * Clips the composable to [PillShape] (50 % rounded corners).
 *
 * Convenience shorthand; combine with [glassBackground] or standalone.
 */
fun Modifier.pillClip(): Modifier = this.clip(PillShape)

// ══════════════════════════════════════════════════════════════════════════════
//  Composable components
// ══════════════════════════════════════════════════════════════════════════════

/**
 * A frosted-glass card surface.
 *
 * Structurally separates the blurred-background layer from the content
 * layer so that children composables are **not** blurred by [glassBackground]:
 *
 *  ```
 *  Box (outer — clip + glass background + border)
 *   ├─ Box (backdrop — blurred glass fill, matchParentSize)
 *   └─ Box (content  — unblurred, receives content lambda)
 *  ```
 *
 * @param modifier       Applied to the outer container.
 * @param shape          Card corner shape (default [CardShape]).
 * @param blurRadius     Backdrop blur radius in pixels (default 24f).
 * @param fillAlpha      White fill opacity (default 0.08f).
 * @param contentPadding Inner padding for [content] (default 16 dp).
 * @param content        Composable content drawn on top of the glass layer.
 */
@Composable
fun GlassCard(
    modifier       : Modifier        = Modifier,
    shape          : Shape           = CardShape,
    blurRadius     : Float           = 24f,
    fillAlpha      : Float           = 0.08f,
    contentPadding : PaddingValues   = PaddingValues(16.dp),
    content        : @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .border(width = 0.5.dp, color = GlassBorder, shape = shape)
    ) {
        // ── Layer 1: frosted glass backdrop ──────────────────────────
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    renderEffect = BlurEffect(
                        radiusX       = blurRadius * 0.4f,
                        radiusY       = blurRadius * 0.4f,
                        edgeTreatment = TileMode.Clamp
                    )
                }
                .background(
                    brush = Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = fillAlpha + 0.06f),
                            1.0f to Color.White.copy(alpha = fillAlpha)
                        ),
                        start = Offset(0f, 0f),
                        end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        )

        // ── Layer 2: content (unblurred) ─────────────────────────────
        Box(
            modifier         = Modifier.padding(contentPadding),
            content          = content
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────

/**
 * A pill-shaped glass button with optional glow ring.
 *
 * Visual anatomy:
 *  - [PillShape] clip
 *  - Frosted glass fill (same [glassBackground] modifier)
 *  - [GlassBorder] outline
 *  - Violet glow via [auraGlow] when [glowEnabled] is true
 *  - Ripple interaction feedback
 *
 * @param text         Button label.
 * @param onClick      Click callback.
 * @param modifier     Applied to the root pill container.
 * @param enabled      Whether the button accepts interactions.
 * @param glowEnabled  Whether to paint an [AuraViolet] glow halo.
 * @param glowColor    Glow halo colour (default [AuraViolet]).
 * @param leadingIcon  Optional composable slotted before the label.
 * @param trailingIcon Optional composable slotted after the label.
 */
@Composable
fun GlassPillButton(
    text         : String,
    onClick      : () -> Unit,
    modifier     : Modifier   = Modifier,
    enabled      : Boolean    = true,
    glowEnabled  : Boolean    = false,
    glowColor    : Color      = AuraViolet,
    leadingIcon  : (@Composable () -> Unit)? = null,
    trailingIcon : (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val baseModifier = modifier
        .wrapContentSize()
        .then(
            if (glowEnabled) Modifier.auraGlow(color = glowColor, radius = 12.dp)
            else Modifier
        )
        .clip(PillShape)
        .background(
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color.White.copy(alpha = 0.12f),
                    1.0f to Color.White.copy(alpha = 0.06f)
                ),
                start = Offset(0f, 0f),
                end   = Offset(Float.POSITIVE_INFINITY, 0f)
            )
        )
        .graphicsLayer {
            renderEffect = BlurEffect(8f, 8f, TileMode.Clamp)
        }
        .border(
            width = 0.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.10f)
                )
            ),
            shape = PillShape
        )
        .clickable(
            interactionSource = interactionSource,
            indication        = ripple(color = glowColor),
            enabled           = enabled,
            onClick           = onClick
        )
        .padding(horizontal = 24.dp, vertical = 12.dp)

    Row(
        modifier         = baseModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                leadingIcon()
            }
        }

        Text(
            text  = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (enabled) TextPrimary else TextSecondary
            )
        )

        if (trailingIcon != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                trailingIcon()
            }
        }
    }
}