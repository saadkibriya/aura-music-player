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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.kibriya.aura.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val artistId: Long,
    val duration: Long,           // milliseconds
    val path: String,
    val uri: String,
    val albumArtUri: String?,
    val trackNumber: Int,
    val year: Int,
    val genre: String?,
    val bitrate: Int,
    val sampleRate: Int,
    val size: Long,               // bytes
    val dateAdded: Long,          // epoch seconds
    val dateModified: Long,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val lastPlayed: Long = 0L     // epoch ms
)

enum class RepeatMode {
    OFF,
    ONE,
    ALL
}

data class PlayerState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffled: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val sleepTimerMs: Long = 0L   // 0 = no timer active
)