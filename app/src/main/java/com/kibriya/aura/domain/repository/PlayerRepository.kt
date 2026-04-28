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

package com.kibriya.aura.domain.repository

import com.kibriya.aura.domain.model.PlayerState
import com.kibriya.aura.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {

    /** Live snapshot of all player state */
    val playerState: StateFlow<PlayerState>

    /** Start playing a specific song, optionally replacing queue */
    suspend fun play(song: Song, queue: List<Song> = listOf(song), startIndex: Int = 0)

    /** Pause current playback */
    suspend fun pause()

    /** Resume from current position */
    suspend fun resume()

    /** Skip to next track (wraps or stops depending on RepeatMode) */
    suspend fun skipNext()

    /** Skip to previous track or restart current if >3 s in */
    suspend fun skipPrevious()

    /** Seek to absolute position in ms */
    suspend fun seekTo(positionMs: Long)

    /** Replace the entire queue; keeps playing if same song is at newIndex */
    suspend fun setQueue(songs: List<Song>, startIndex: Int = 0)

    /** Toggle shuffle; rebuilds queue order accordingly */
    suspend fun toggleShuffle()

    /** Cycle OFF → ONE → ALL → OFF */
    suspend fun cycleRepeatMode()

    /**
     * Start a sleep timer.
     * @param durationMs Time in ms until playback fades out and stops.
     */
    suspend fun setSleepTimer(durationMs: Long)

    /** Cancel any active sleep timer */
    suspend fun cancelSleepTimer()
}