// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.nowplaying.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kibriya.aura.ui.theme.AuraViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AlbumArtShape = RoundedCornerShape(24.dp)

@Composable
fun AlbumArtSection(
    albumArtUri: String?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    dominantColor: Color,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val artScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.95f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "albumArtScale"
    )

    var showHeartBurst by remember { mutableStateOf(false) }
    val heartAlpha by animateFloatAsState(
        targetValue = if (showHeartBurst) 1f else 0f,
        animationSpec = tween(200), label = "heartAlpha"
    )
    val heartScale by animateFloatAsState(
        targetValue = if (showHeartBurst) 1.4f else 0.6f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "heartScale"
    )

    var totalDragX by remember { mutableStateOf(0f) }
    val swipeThreshold = 80f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(artScale)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onDragEnd = {
                        when {
                            totalDragX < -swipeThreshold -> onSwipeLeft()
                            totalDragX > swipeThreshold  -> onSwipeRight()
                        }
                        totalDragX = 0f
                    },
                    onDragCancel = { totalDragX = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDragX += dragAmount }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onToggleFavorite()
                        coroutineScope.launch {
                            showHeartBurst = true
                            delay(600)
                            showHeartBurst = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .shadow(
                    elevation = 32.dp,
                    shape = AlbumArtShape,
                    ambientColor = dominantColor.copy(alpha = 0.5f),
                    spotColor = AuraViolet.copy(alpha = 0.6f)
                )
        )

        if (albumArtUri != null) {
            AsyncImage(
                model = albumArtUri,
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(AlbumArtShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(AlbumArtShape)
                    .background(Color(0xFF1C1C28)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AuraViolet.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxSize(0.4f)
                )
            }
        }

        if (showHeartBurst || heartAlpha > 0f) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = heartAlpha),
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = heartScale
                        scaleY = heartScale
                        alpha = heartAlpha
                    }
            )
        }
    }
}