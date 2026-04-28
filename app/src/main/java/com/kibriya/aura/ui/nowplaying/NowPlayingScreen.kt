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

package com.kibriya.aura.ui.nowplaying

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.ui.nowplaying.components.*
import com.kibriya.aura.ui.theme.glassBackground

@Composable
fun NowPlayingScreen(
    onNavigateBack: () -> Unit,
    onOpenQueue: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val positionMs by viewModel.positionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isShuffled by viewModel.isShuffled.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()

    val isFavorite by remember(currentSong) {
        derivedStateOf { currentSong?.isFavorite ?: false }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Animated mesh gradient background — fills entire screen
        MeshGradientBackground(
            dominantColor = dominantColor,
            modifier = Modifier.fillMaxSize()
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Color.White
                )

                IconButton(onClick = onOpenQueue) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Album art — squircle, full-width with padding
            AlbumArtSection(
                albumArtUri = currentSong?.albumArtUri,
                isPlaying = isPlaying,
                isFavorite = isFavorite,
                dominantColor = dominantColor,
                onSwipeLeft = { viewModel.skipNext() },
                onSwipeRight = { viewModel.skipPrevious() },
                onToggleFavorite = { viewModel.toggleFavorite() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Song info — title, artist, favorite button
            SongInfoSection(
                title = currentSong?.title ?: "No song playing",
                artist = currentSong?.artist ?: "Unknown artist",
                isFavorite = isFavorite,
                onToggleFavorite = { viewModel.toggleFavorite() },
                modifier = Modifier
                    .fillMaxWidth()
                    .glassBackground()
                    .padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Progress bar — custom drawn
            ProgressBarSection(
                positionMs = positionMs,
                durationMs = durationMs,
                dominantColor = dominantColor,
                onSeek = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Playback controls — previous, play/pause, next, shuffle, repeat
            PlayerControlsSection(
                isPlaying = isPlaying,
                isShuffled = isShuffled,
                repeatMode = repeatMode,
                onPlay = { viewModel.play() },
                onPause = { viewModel.pause() },
                onSkipNext = { viewModel.skipNext() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onCycleRepeat = { viewModel.cycleRepeat() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Volume slider — AudioManager backed
            VolumeSection(
                dominantColor = dominantColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}