// MIT License
// Copyright (c) 2024 Project Aura

package com.kibriya.aura.domain.repository

import com.kibriya.aura.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getAlbums(): Flow<List<Song>>
    fun getArtists(): Flow<List<String>>
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsByArtist(artist: String): Flow<List<Song>>
    fun getMostPlayed(): Flow<List<Song>>
    fun getRecentlyAdded(): Flow<List<Song>>
    fun getFavorites(): Flow<List<Song>>
    suspend fun toggleFavorite(songId: Long)
    suspend fun updatePlayCount(songId: Long)
    suspend fun triggerScan()
}