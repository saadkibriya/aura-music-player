// MIT License
//
// Copyright (c) 2024 Saad Kibriya
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

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
            playerRepository.playerState().collect { state ->
                _playerState.value = state
            }
        }
    }

    fun play(song: Song) {
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
        viewModelScope.launch { playerRepository.cycleRepeat() }
    }

    fun toggleFavorite(songId: Long) {
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