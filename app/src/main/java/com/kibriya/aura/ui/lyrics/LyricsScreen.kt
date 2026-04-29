// MIT License
// Copyright (c) 2025 Md Golam Kibriya
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.

package com.kibriya.aura.ui.lyrics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.ui.components.GlassComponents.auraGlow
import com.kibriya.aura.ui.nowplaying.MeshGradientBackground
import com.kibriya.aura.ui.nowplaying.NowPlayingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsScreen(
    onBack: () -> Unit,
    lyricsViewModel: LyricsViewModel = hiltViewModel(),
    nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()
) {
    val lyrics by lyricsViewModel.lyrics.collectAsState()
    val activeIndex by lyricsViewModel.activeLyricIndex.collectAsState()
    val hasLyrics by lyricsViewModel.hasLyrics.collectAsState()
    val dominantColor by nowPlayingViewModel.dominantColor.collectAsState()
    val songTitle by nowPlayingViewModel.currentSongTitle.collectAsState()

    val listState = rememberLazyListState()

    // Auto-scroll to active lyric line whenever it changes
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && hasLyrics) {
            listState.animateScrollToItem(
                index = (activeIndex - 2).coerceAtLeast(0),
                scrollOffset = 0
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen mesh gradient using dominant album art color
        MeshGradientBackground(dominantColor = dominantColor)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = songTitle ?: "Lyrics",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            if (!hasLyrics) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No lyrics available",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 80.dp)
                ) {
                    itemsIndexed(lyrics) { index, line ->
                        LyricLineItem(
                            text = line.text,
                            isActive = line.isActive,
                            positionRelativeToActive = when {
                                index == activeIndex -> 0
                                index < activeIndex -> -1  // above active
                                else -> 1                  // below active
                            },
                            dominantColor = dominantColor,
                            onClick = { lyricsViewModel.seekToLine(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricLineItem(
    text: String,
    isActive: Boolean,
    positionRelativeToActive: Int, // -1 above, 0 active, 1 below
    dominantColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "lyric_scale"
    )

    val (textColor, fontSize, fontWeight) = when {
        isActive -> Triple(
            Color.White,
            22.sp,
            FontWeight.Bold
        )
        positionRelativeToActive < 0 -> Triple(  // lines above active
            Color.White.copy(alpha = 0.4f),
            16.sp,
            FontWeight.Normal
        )
        else -> Triple(                            // lines below active
            Color.White.copy(alpha = 0.6f),
            16.sp,
            FontWeight.Normal
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (isActive) Modifier.auraGlow(color = dominantColor, radius = 16.dp)
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            lineHeight = (fontSize.value * 1.4f).sp,
            textAlign = TextAlign.Start
        )
    }
}