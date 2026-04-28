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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import com.kibriya.aura.ui.theme.AuraBlack
import com.kibriya.aura.ui.theme.AuraViolet

@Composable
fun MeshGradientBackground(
    dominantColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "meshGradient")

    // Layer 1 — dominant color blob, drifts top-left → top-right
    val layer1X by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "layer1X"
    )
    val layer1Y by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "layer1Y"
    )

    // Layer 2 — violet blob, drifts center → bottom-right
    val layer2X by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "layer2X"
    )
    val layer2Y by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "layer2Y"
    )

    // Layer 3 — dominant color secondary blob, bottom area breathing
    val layer3X by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "layer3X"
    )
    val layer3Y by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "layer3Y"
    )

    // Use fallback if dominant is too dark or same as AuraBlack
    val effectiveDominant = remember(dominantColor) {
        if (dominantColor == AuraBlack || dominantColor == Color.Transparent) AuraViolet
        else dominantColor
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base — AuraBlack fill
        drawRect(color = AuraBlack)

        // Layer 1 — dominant blob (top area, 20% alpha)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    effectiveDominant.copy(alpha = 0.22f),
                    Color.Transparent
                ),
                center = Offset(layer1X * w, layer1Y * h),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(layer1X * w, layer1Y * h)
        )

        // Layer 2 — violet blob (right/mid area, 18% alpha)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AuraViolet.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(layer2X * w, layer2Y * h),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(layer2X * w, layer2Y * h)
        )

        // Layer 3 — dominant color, bottom/breathing, 15% alpha
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    effectiveDominant.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(layer3X * w, layer3Y * h),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(layer3X * w, layer3Y * h)
        )
    }
}