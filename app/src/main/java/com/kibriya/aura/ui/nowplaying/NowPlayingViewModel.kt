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

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.kibriya.aura.data.repository.PlayerRepository
import com.kibriya.aura.data.repository.SongRepository
import com.kibriya.aura.data.db.entity.SongEntity
import com.kibriya.aura.domain.model.PlayerState
import com.kibriya.aura.ui.theme.AuraViolet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _playerState: StateFlow<PlayerState> = playerRepository.playerState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerState())

    val currentSong: StateFlow<SongEntity?> = _playerState
        .map { it.currentSong }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isPlaying: StateFlow<Boolean> = _playerState
        .map { it.isPlaying }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val positionMs: StateFlow<Long> = _playerState
        .map { it.positionMs }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val durationMs: StateFlow<Long> = _playerState
        .map { it.durationMs }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val repeatMode: StateFlow<Int> = _playerState
        .map { it.repeatMode }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerState.REPEAT_MODE_OFF)

    val isShuffled: StateFlow<Boolean> = _playerState
        .map { it.isShuffled }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val sleepTimerMs: StateFlow<Long?> = _playerState
        .map { it.sleepTimerMs }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _dominantColor = MutableStateFlow(AuraViolet)
    val dominantColor: StateFlow<Color> = _dominantColor.asStateFlow()

    private val _vibrantColor = MutableStateFlow(AuraViolet)
    val vibrantColor: StateFlow<Color> = _vibrantColor.asStateFlow()

    init {
        observeAlbumArtForPalette()
    }

    private fun observeAlbumArtForPalette() {
        viewModelScope.launch {
            currentSong.collectLatest { song ->
                if (song?.albumArtBitmap != null) {
                    extractPaletteColors(song.albumArtBitmap)
                } else {
                    _dominantColor.value = AuraViolet
                    _vibrantColor.value = AuraViolet
                }
            }
        }
    }

    private suspend fun extractPaletteColors(bitmap: Bitmap) {
        withContext(Dispatchers.Default) {
            try {
                val palette = Palette.from(bitmap).generate()
                val dominant = palette.getDominantColor(android.graphics.Color.TRANSPARENT)
                val vibrant = palette.getVibrantColor(android.graphics.Color.TRANSPARENT)
                if (dominant != android.graphics.Color.TRANSPARENT) {
                    _dominantColor.value = Color(dominant)
                }
                if (vibrant != android.graphics.Color.TRANSPARENT) {
                    _vibrantColor.value = Color(vibrant)
                }
            } catch (e: Exception) {
                _dominantColor.value = AuraViolet
                _vibrantColor.value = AuraViolet
            }
        }
    }

    fun play() {
        viewModelScope.launch { playerRepository.play() }
    }

    fun pause() {
        viewModelScope.launch { playerRepository.pause() }
    }

    fun skipNext() {
        viewModelScope.launch { playerRepository.skipNext() }
    }

    fun skipPrevious() {
        viewModelScope.launch { playerRepository.skipPrevious() }
    }

    fun seekTo(positionMs: Long) {
        viewModelScope.launch { playerRepository.seekTo(positionMs) }
    }

    fun toggleShuffle() {
        viewModelScope.launch { playerRepository.toggleShuffle() }
    }

    fun cycleRepeat() {
        viewModelScope.launch { playerRepository.cycleRepeat() }
    }

    fun setSleepTimer(durationMs: Long?) {
        viewModelScope.launch { playerRepository.setSleepTimer(durationMs) }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            currentSong.value?.id?.let { songId ->
                songRepository.toggleFavorite(songId)
            }
        }
    }
}