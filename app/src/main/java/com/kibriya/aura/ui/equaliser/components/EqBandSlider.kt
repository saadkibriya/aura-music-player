/*
 * MIT License
 * Copyright (c) 2025 Md Golam Kibriya
 */

package com.kibriya.aura.ui.equaliser.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VioletAccent = Color(0xFF8B5CF6)
private val TrackInactive = Color(0xFF1E1B2E)
private val TrackActive = Color(0xFF2D2550)
private val ThumbColor = Color(0xFFFFFFFF)
private val LabelColor = Color(0xFFB0A8C8)

/**
 * Vertical EQ band slider with Canvas-drawn pill track and violet glow on the thumb
 * when gain != 0dB.
 *
 * @param gainDb    Current band gain in dB (-12..+12)
 * @param label     Frequency label (e.g. "1kHz")
 * @param onGainChange  Callback with new gain when user drags
 */
@Composable
fun EqBandSlider(
    gainDb: Float,
    label: String,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val minDb = -12f
    val maxDb = 12f
    val range = maxDb - minDb

    // Animate thumb position fraction (0 = bottom = -12dB, 1 = top = +12dB)
    val fraction by animateFloatAsState(
        targetValue = (gainDb - minDb) / range,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "eqThumb"
    )

    val isActive = gainDb != 0f
    val dbLabel = when {
        gainDb > 0f -> "+${gainDb.toInt()}"
        gainDb == 0f -> "0"
        else -> gainDb.toInt().toString()
    }

    Column(
        modifier = modifier.width(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // dB value above slider
        Text(
            text = dbLabel,
            color = if (isActive) VioletAccent else LabelColor,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(14.dp)
        )

        Spacer(Modifier.height(4.dp))

        // Canvas slider track
        var trackHeightPx by remember { mutableFloatStateOf(0f) }

        Canvas(
            modifier = Modifier
                .width(16.dp)
                .weight(1f)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        val heightPx = trackHeightPx.takeIf { it > 0f } ?: size.height.toFloat()
                        val delta = -dragAmount / heightPx * range
                        val newGain = (gainDb + delta).coerceIn(minDb, maxDb)
                        // Snap to 0 if within 0.5dB
                        val snapped = if (kotlin.math.abs(newGain) < 0.5f) 0f else newGain
                        onGainChange(snapped)
                    }
                }
        ) {
            trackHeightPx = size.height
            drawEqTrack(fraction, isActive, size)
        }

        Spacer(Modifier.height(6.dp))

        // Frequency label below
        Text(
            text = label,
            color = LabelColor,
            fontSize = 8.sp,
            textAlign = TextAlign.Center,
            lineHeight = 9.sp
        )
    }
}

private fun DrawScope.drawEqTrack(
    fraction: Float,
    isActive: Boolean,
    canvasSize: Size
) {
    val trackWidth = canvasSize.width * 0.45f
    val trackLeft = (canvasSize.width - trackWidth) / 2f
    val cornerRadius = CornerRadius(trackWidth / 2f)

    // Background pill track
    drawRoundRect(
        color = TrackInactive,
        topLeft = Offset(trackLeft, 0f),
        size = Size(trackWidth, canvasSize.height),
        cornerRadius = cornerRadius
    )

    // Active fill: center (0dB) to thumb
    val centerY = canvasSize.height / 2f
    val thumbY = canvasSize.height * (1f - fraction)
    val fillTop = minOf(thumbY, centerY)
    val fillBottom = maxOf(thumbY, centerY)

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                VioletAccent.copy(alpha = 0.9f),
                VioletAccent.copy(alpha = 0.5f)
            ),
            startY = fillTop,
            endY = fillBottom
        ),
        topLeft = Offset(trackLeft, fillTop),
        size = Size(trackWidth, (fillBottom - fillTop).coerceAtLeast(2f)),
        cornerRadius = cornerRadius
    )

    // Center marker line
    drawLine(
        color = Color(0xFF4A4060),
        start = Offset(trackLeft - 2f, centerY),
        end = Offset(trackLeft + trackWidth + 2f, centerY),
        strokeWidth = 1f
    )

    // Thumb
    val thumbRadius = trackWidth * 0.85f
    val thumbCx = canvasSize.width / 2f

    if (isActive) {
        // Glow ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VioletAccent.copy(alpha = 0.45f),
                    Color.Transparent
                ),
                center = Offset(thumbCx, thumbY),
                radius = thumbRadius * 2.2f
            ),
            radius = thumbRadius * 2.2f,
            center = Offset(thumbCx, thumbY)
        )
    }

    // Thumb fill
    drawCircle(
        color = if (isActive) ThumbColor else Color(0xFF7A7090),
        radius = thumbRadius,
        center = Offset(thumbCx, thumbY)
    )

    // Thumb border
    drawCircle(
        color = if (isActive) VioletAccent else Color(0xFF4A4060),
        radius = thumbRadius,
        center = Offset(thumbCx, thumbY),
        style = Stroke(width = 1.5f)
    )
}