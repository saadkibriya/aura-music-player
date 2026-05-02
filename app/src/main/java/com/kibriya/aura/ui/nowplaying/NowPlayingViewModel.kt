// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.nowplaying

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.kibriya.aura.domain.model.PlayerState
import com.kibriya.aura.domain.model.Song
import com.kibriya.aura.domain.repository.PlayerRepository
import com.kibriya.aura.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    val dominantColor = MutableStateFlow(Color(0xFF8B5CF6))

    init {
        viewModelScope.launch {
            playerRepository.playerState.collect { state ->
                _playerState.value = state
            }
        }
    }

    fun play(song: Song) {
        viewModelScope.launch { playerRepository.play(song) }
    }

    fun play() {
        val song = _playerState.value.currentSong ?: return
        viewModelScope.launch { playerRepository.play(song) }
    }

    fun pause() {
        viewModelScope.launch { playerRepository.pause() }
    }

    fun resume() {
        viewModelScope.launch { playerRepository.resume() }
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
        viewModelScope.launch { playerRepository.cycleRepeatMode() }
    }

    fun toggleFavorite() {
        val songId = _playerState.value.currentSong?.id ?: return
        viewModelScope.launch { songRepository.toggleFavorite(songId) }
    }

    fun updateDominantColor(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            val swatch = palette?.dominantSwatch
            if (swatch != null) {
                dominantColor.value = Color(swatch.rgb)
            }
        }
    }
}