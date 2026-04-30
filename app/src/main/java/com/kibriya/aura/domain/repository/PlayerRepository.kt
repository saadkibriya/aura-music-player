// MIT License
// Copyright (c) 2024 Project Aura

package com.kibriya.aura.domain.repository

import com.kibriya.aura.domain.model.PlayerState
import com.kibriya.aura.domain.model.RepeatMode
import com.kibriya.aura.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    val playerState: StateFlow<PlayerState>
    suspend fun play(song: Song)
    suspend fun pause()
    suspend fun resume()
    suspend fun skipNext()
    suspend fun skipPrevious()
    suspend fun seekTo(positionMs: Long)
    suspend fun setQueue(songs: List<Song>, startIndex: Int)
    suspend fun toggleShuffle()
    suspend fun cycleRepeatMode()
    suspend fun setSleepTimer(durationMs: Long)
    suspend fun cancelSleepTimer()
}