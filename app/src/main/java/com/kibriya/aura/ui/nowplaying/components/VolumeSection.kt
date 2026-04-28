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
import androidx.compose.ui.unit.dp
import com.kibriya.aura.ui.theme.AuraViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VolumeSection(
    dominantColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

    var volumeProgress by remember {
        mutableStateOf(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume.toFloat()
        )
    }

    val coroutineScope = rememberCoroutineScope()

    // Poll actual volume every 500ms to stay in sync with hardware buttons
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVolume.toFloat()
            volumeProgress = current
        }
    }

    val trackHeight = 6.dp
    val thumbRadius = 10.dp
    val glowColor = dominantColor.copy(alpha = 0.5f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.VolumeDown,
            contentDescription = "Volume low",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(thumbRadius * 2 + 4.dp)
                .pointerInput(maxVolume) {
                    detectTapGestures { offset ->
                        val progress = (offset.x / size.width).coerceIn(0f, 1f)
                        volumeProgress = progress
                        audioManager.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            (progress * maxVolume).toInt(),
                            0
                        )
                    }
                }
                .pointerInput(maxVolume) {
                    detectDragGestures { change, _ ->
                        val progress = (change.position.x / size.width).coerceIn(0f, 1f)
                        volumeProgress = progress
                        audioManager.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            (progress * maxVolume).toInt(),
                            0
                        )
                    }
                }
        ) {
            val trackHeightPx = trackHeight.toPx()
            val thumbRadiusPx = thumbRadius.toPx()
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val centerY = heightPx / 2f
            val cornerRadius = trackHeightPx / 2f
            val filledWidth = (volumeProgress * widthPx).coerceIn(0f, widthPx)
            val thumbX = filledWidth.coerceIn(thumbRadiusPx, widthPx - thumbRadiusPx)

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Track background
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.15f),
                    topLeft = Offset(0f, centerY - trackHeightPx / 2f),
                    size = Size(widthPx, trackHeightPx),
                    cornerRadius = CornerRadius(cornerRadius)
                )

                // Glow
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

                // Filled track
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
                    radius = thumbRadiusPx * 1.6f,
                    center = Offset(thumbX, centerY)
                )

                // Thumb
                drawCircle(
                    color = Color.White,
                    radius = thumbRadiusPx,
                    center = Offset(thumbX, centerY)
                )
            }
        }

        Icon(
            imageVector = Icons.Rounded.VolumeUp,
            contentDescription = "Volume high",
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}