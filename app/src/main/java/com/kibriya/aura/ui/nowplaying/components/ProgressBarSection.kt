/*
 * MIT License
 * Copyright (c) 2025 Md Golam Kibriya
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 */

package com.kibriya.aura.ui.nowplaying.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibriya.aura.ui.theme.AuraViolet
import com.kibriya.aura.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit

@Composable
fun ProgressBarSection(
    positionMs: Long,
    durationMs: Long,
    dominantColor: Color,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackHeight = 6.dp
    val thumbRadius = 10.dp
    val thumbRadiusExpanded = 14.dp

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(0f) }

    // Smooth animated progress (1 second animation when not dragging)
    val animatedProgress by animateFloatAsState(
        targetValue = if (durationMs > 0L) {
            if (isDragging) dragProgress else (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f,
        animationSpec = if (isDragging) snap() else tween(durationMillis = 1000, easing = LinearEasing),
        label = "progressAnim"
    )

    // Thumb size animation on drag
    val currentThumbRadius by animateDpAsState(
        targetValue = if (isDragging) thumbRadiusExpanded else thumbRadius,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "thumbRadius"
    )

    val glowColor = dominantColor.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Custom progress bar canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height((thumbRadiusExpanded * 2))
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        if (durationMs > 0L) {
                            val progress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((progress * durationMs).toLong())
                        }
                    }
                }
                .pointerInput(durationMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            if (durationMs > 0L) {
                                onSeek((dragProgress * durationMs).toLong())
                            }
                            isDragging = false
                        },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, _ ->
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            val thumbRadiusPx = currentThumbRadius.toPx()
            val trackHeightPx = trackHeight.toPx()
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val centerY = heightPx / 2f
            val cornerRadius = trackHeightPx / 2f
            val filledWidth = (animatedProgress * widthPx).coerceIn(0f, widthPx)
            val thumbX = filledWidth.coerceIn(thumbRadiusPx, widthPx - thumbRadiusPx)

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Track background
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.15f),
                    topLeft = Offset(0f, centerY - trackHeightPx / 2f),
                    size = Size(widthPx, trackHeightPx),
                    cornerRadius = CornerRadius(cornerRadius)
                )

                // Glow effect under filled track
                drawIntoCanvas { canvas ->
                    val glowPaint = Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(
                                thumbRadiusPx * 0.8f,
                                0f, 0f,
                                glowColor.copy(alpha = 0.8f).toArgb()
                            )
                        }
                    }
                    canvas.drawRoundRect(
                        left = 0f,
                        top = centerY - trackHeightPx / 2f,
                        right = filledWidth,
                        bottom = centerY + trackHeightPx / 2f,
                        radiusX = cornerRadius,
                        radiusY = cornerRadius,
                        paint = glowPaint
                    )
                }

                // Filled track with gradient
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(dominantColor, AuraViolet),
                        startX = 0f,
                        endX = filledWidth.coerceAtLeast(1f)
                    ),
                    topLeft = Offset(0f, centerY - trackHeightPx / 2f),
                    size = Size(filledWidth, trackHeightPx),
                    cornerRadius = CornerRadius(cornerRadius)
                )

                // Thumb glow
                drawCircle(
                    color = AuraViolet.copy(alpha = 0.3f),
                    radius = thumbRadiusPx * 1.8f,
                    center = Offset(thumbX, centerY)
                )

                // Thumb pill
                drawCircle(
                    color = Color.White,
                    radius = thumbRadiusPx,
                    center = Offset(thumbX, centerY)
                )
            }
        }

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(if (isDragging) (dragProgress * durationMs).toLong() else positionMs),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return "%d:%02d".format(minutes, seconds)
}