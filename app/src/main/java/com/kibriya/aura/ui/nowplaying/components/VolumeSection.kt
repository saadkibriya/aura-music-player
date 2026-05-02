
// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.nowplaying.components

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kibriya.aura.ui.theme.AuraViolet
import kotlinx.coroutines.delay

@Composable
fun VolumeSection(
    dominantColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    var volumeProgress by remember {
        mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            volumeProgress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume
        }
    }

    val trackHeight = 6.dp
    val thumbRadius = 10.dp
    val glowColor = dominantColor.copy(alpha = 0.5f)

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Rounded.VolumeDown, "Volume low",
            tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(thumbRadius * 2 + 4.dp)
                .pointerInput(maxVolume) {
                    detectTapGestures { offset ->
                        val progress = (offset.x / size.width).coerceIn(0f, 1f)
                        volumeProgress = progress
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            (progress * maxVolume).toInt(), 0)
                    }
                }
                .pointerInput(maxVolume) {
                    detectDragGestures { change, _ ->
                        val progress = (change.position.x / size.width).coerceIn(0f, 1f)
                        volumeProgress = progress
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            (progress * maxVolume).toInt(), 0)
                    }
                }
        ) {
            val trackHeightPx = with(density) { trackHeight.toPx() }
            val thumbRadiusPx = with(density) { thumbRadius.toPx() }
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val centerY = heightPx / 2f
            val cornerRadius = trackHeightPx / 2f
            val filledWidth = (volumeProgress * widthPx).coerceIn(0f, widthPx)
            val thumbX = filledWidth.coerceIn(thumbRadiusPx, widthPx - thumbRadiusPx)

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(Color.White.copy(alpha = 0.15f),
                    Offset(0f, centerY - trackHeightPx / 2f),
                    Size(widthPx, trackHeightPx), CornerRadius(cornerRadius))
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
                    brush = Brush.horizontalGradient(listOf(dominantColor, AuraViolet),
                        0f, filledWidth.coerceAtLeast(1f)),
                    topLeft = Offset(0f, centerY - trackHeightPx / 2f),
                    size = Size(filledWidth, trackHeightPx),
                    cornerRadius = CornerRadius(cornerRadius)
                )
                drawCircle(AuraViolet.copy(alpha = 0.3f), thumbRadiusPx * 1.6f, Offset(thumbX, centerY))
                drawCircle(Color.White, thumbRadiusPx, Offset(thumbX, centerY))
            }
        }

        Icon(Icons.Rounded.VolumeUp, "Volume high",
            tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
    }
}