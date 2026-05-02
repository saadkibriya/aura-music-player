// MIT License
// Copyright (c) 2025 Md Golam Kibriya
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
import com.kibriya.aura.domain.model.RepeatMode
import com.kibriya.aura.ui.nowplaying.components.*
import com.kibriya.aura.ui.theme.glassBackground

@Composable
fun NowPlayingScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val dominantColor by viewModel.dominantColor.collectAsState()

    val currentSong = playerState.currentSong
    val isPlaying = playerState.isPlaying
    val positionMs = playerState.positionMs
    val durationMs = playerState.durationMs
    val repeatMode = playerState.repeatMode
    val isShuffled = playerState.isShuffled
    val isFavorite = currentSong?.isFavorite ?: false

    Box(modifier = modifier.fillMaxSize()) {
        MeshGradientBackground(
            dominantColor = dominantColor,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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

            ProgressBarSection(
                positionMs = positionMs,
                durationMs = durationMs,
                dominantColor = dominantColor,
                onSeek = { viewModel.seekTo(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            VolumeSection(
                dominantColor = dominantColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}