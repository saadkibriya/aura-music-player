// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.nowplaying.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import com.kibriya.aura.ui.theme.AuraBackground
import com.kibriya.aura.ui.theme.AuraViolet

@Composable
fun MeshGradientBackground(
    dominantColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "meshGradient")

    val layer1X by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.45f,
        animationSpec = infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "layer1X"
    )
    val layer1Y by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(9500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "layer1Y"
    )
    val layer2X by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(11000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "layer2X"
    )
    val layer2Y by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(7500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "layer2Y"
    )
    val layer3X by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(13000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "layer3X"
    )
    val layer3Y by infiniteTransition.animateFloat(
        initialValue = 0.75f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(10000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "layer3Y"
    )

    val effectiveDominant = remember(dominantColor) {
        if (dominantColor == AuraBackground || dominantColor == Color.Transparent) AuraViolet
        else dominantColor
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(color = AuraBackground)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(effectiveDominant.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(layer1X * w, layer1Y * h), radius = w * 0.65f
            ),
            radius = w * 0.65f, center = Offset(layer1X * w, layer1Y * h)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AuraViolet.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(layer2X * w, layer2Y * h), radius = w * 0.55f
            ),
            radius = w * 0.55f, center = Offset(layer2X * w, layer2Y * h)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(effectiveDominant.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(layer3X * w, layer3Y * h), radius = w * 0.5f
            ),
            radius = w * 0.5f, center = Offset(layer3X * w, layer3Y * h)
        )
    }
}