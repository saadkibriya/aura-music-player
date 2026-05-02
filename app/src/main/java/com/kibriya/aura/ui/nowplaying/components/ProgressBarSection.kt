// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.nowplaying.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
    val density = LocalDensity.current

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = if (durationMs > 0L) {
            if (isDragging) dragProgress
            else (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f,
        animationSpec = if (isDragging) snap() else tween(1000, easing = LinearEasing),
        label = "progressAnim"
    )

    val currentThumbRadius by animateDpAsState(
        targetValue = if (isDragging) thumbRadiusExpanded else thumbRadius,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "thumbRadius"
    )

    val glowColor = dominantColor.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbRadiusExpanded * 2)
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
                            if (durationMs > 0L) onSeek((dragProgress * durationMs).toLong())
                            isDragging = false
                        },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, _ ->
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            val thumbRadiusPx = with(density) { currentThumbRadius.toPx() }
            val trackHeightPx = with(density) { trackHeight.toPx() }
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val centerY = heightPx / 2f
            val cornerRadius = trackHeightPx / 2f
            val filledWidth = (animatedProgress * widthPx).coerceIn(0f, widthPx)
            val thumbX = filledWidth.coerceIn(thumbRadiusPx, widthPx - thumbRadiusPx)

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.15f),
                    topLeft = Offset(0f, centerY - trackHeightPx / 2f),
                    size = Size(widthPx, trackHeightPx),
                    cornerRadius = CornerRadius(cornerRadius)
                )
                drawIntoCanvas { canvas ->
                    val glowPaint = Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(thumbRadiusPx * 0.8f, 0f, 0f,
                                glowColor.copy(alpha = 0.8f).toArgb())
                        }
                    }
                    canvas.drawRoundRect(0f, centerY - trackHeightPx / 2f,
                        filledWidth, centerY + trackHeightPx / 2f,
                        cornerRadius, cornerRadius, glowPaint)
                }
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(dominantColor, AuraViolet), 0f, filledWidth.coerceAtLeast(1f)
                    ),
                    topLeft = Offset(0f, centerY - trackHeightPx / 2f),
                    size = Size(filledWidth, trackHeightPx),
                    cornerRadius = CornerRadius(cornerRadius)
                )
                drawCircle(AuraViolet.copy(alpha = 0.3f), thumbRadiusPx * 1.8f, Offset(thumbX, centerY))
                drawCircle(Color.White, thumbRadiusPx, Offset(thumbX, centerY))
            }
        }

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