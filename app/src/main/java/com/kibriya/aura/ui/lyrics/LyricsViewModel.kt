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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.lyrics.LrcParser
import com.kibriya.aura.data.repository.PlayerRepository
import com.kibriya.aura.domain.model.LyricLine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    // Raw parsed lyrics without isActive state
    private val _rawLyrics = MutableStateFlow<List<LyricLine>>(emptyList())

    private val _activeLyricIndex = MutableStateFlow(-1)
    val activeLyricIndex: StateFlow<Int> = _activeLyricIndex.asStateFlow()

    val hasLyrics: StateFlow<Boolean> = _rawLyrics
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // Emits lyrics list with isActive flag applied
    val lyrics: StateFlow<List<LyricLine>> = combine(_rawLyrics, _activeLyricIndex) { lines, activeIdx ->
        lines.mapIndexed { idx, line -> line.copy(isActive = idx == activeIdx) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        observeCurrentSong()
        startPositionSync()
    }

    /** Reload lyrics whenever the current song changes. */
    private fun observeCurrentSong() {
        viewModelScope.launch {
            playerRepository.currentSongPath
                .distinctUntilChanged()
                .collect { path ->
                    if (path.isNullOrBlank()) {
                        _rawLyrics.value = emptyList()
                        _activeLyricIndex.value = -1
                        return@collect
                    }
                    val content = LrcParser.findLrcFile(path)
                    _rawLyrics.value = if (content != null) LrcParser.parse(content) else emptyList()
                    _activeLyricIndex.value = -1
                }
        }
    }

    /**
     * Polls playback position every 500 ms and finds the last lyric line
     * whose timestamp <= current position. Binary search for efficiency.
     */
    private fun startPositionSync() {
        viewModelScope.launch {
            while (true) {
                delay(500)
                val positionMs = playerRepository.positionMs.value
                val lines = _rawLyrics.value
                if (lines.isEmpty()) continue

                // Binary search: find last index with timestampMs <= positionMs
                var lo = 0; var hi = lines.lastIndex; var result = -1
                while (lo <= hi) {
                    val mid = (lo + hi) / 2
                    if (lines[mid].timestampMs <= positionMs) {
                        result = mid; lo = mid + 1
                    } else {
                        hi = mid - 1
                    }
                }
                if (result != _activeLyricIndex.value) {
                    _activeLyricIndex.value = result
                }
            }
        }
    }

    /** Seek playback to the timestamp of the tapped lyric line. */
    fun seekToLine(index: Int) {
        val lines = _rawLyrics.value
        if (index < 0 || index >= lines.size) return
        playerRepository.seekTo(lines[index].timestampMs)
    }
}