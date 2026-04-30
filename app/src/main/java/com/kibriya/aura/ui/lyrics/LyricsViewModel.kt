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

package com.kibriya.aura.ui.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.lyrics.LrcParser
import com.kibriya.aura.domain.model.LyricLine
import com.kibriya.aura.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()

    private val _activeLyricIndex = MutableStateFlow(0)
    val activeLyricIndex: StateFlow<Int> = _activeLyricIndex.asStateFlow()

    private val _hasLyrics = MutableStateFlow(false)
    val hasLyrics: StateFlow<Boolean> = _hasLyrics.asStateFlow()

    init {
        viewModelScope.launch {
            playerRepository.currentSong().collect { song ->
                if (song != null) {
                    val parsed = LrcParser.parse(song.path)
                    _lyrics.value = parsed
                    _hasLyrics.value = parsed.isNotEmpty()
                } else {
                    _lyrics.value = emptyList()
                    _hasLyrics.value = false
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(500)
                val currentPosition = playerRepository.positionMs()
                val lyricList = _lyrics.value
                if (lyricList.isNotEmpty()) {
                    val index = lyricList.indexOfLast { it.timeMs <= currentPosition }
                    if (index >= 0) _activeLyricIndex.value = index
                }
            }
        }
    }

    fun seekToLine(index: Int) {
        viewModelScope.launch {
            val line = _lyrics.value.getOrNull(index) ?: return@launch
            playerRepository.seekTo(line.timeMs)
        }
    }
}