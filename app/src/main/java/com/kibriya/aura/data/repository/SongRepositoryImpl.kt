// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.data.repository

import android.content.Context
import com.kibriya.aura.data.local.dao.SongDao
import com.kibriya.aura.data.local.entities.SongEntity
import com.kibriya.aura.domain.model.Song
import com.kibriya.aura.domain.repository.SongRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    @ApplicationContext private val context: Context
) : SongRepository {

    private fun SongEntity.toDomain() = Song(
        id           = id,
        title        = title,
        artist       = artist,
        album        = album,
        albumId      = albumId,
        artistId     = 0L,
        duration     = duration,
        path         = filePath,
        uri          = filePath,
        albumArtUri  = albumArtUri,
        trackNumber  = 0,
        year         = 0,
        genre        = null,
        bitrate      = 0,
        sampleRate   = 0,
        size         = 0L,
        dateAdded    = dateAdded,
        dateModified = 0L,
        playCount    = playCount,
        isFavorite   = isFavorite,
        lastPlayed   = 0L
    )

    override fun getAllSongs(): Flow<List<Song>> =
        songDao.getAllSongs().map { it.map { e -> e.toDomain() } }

    override fun getAlbums(): Flow<List<Song>> =
        songDao.getAllSongs().map { it.distinctBy { e -> e.albumId }.map { e -> e.toDomain() } }

    override fun getArtists(): Flow<List<String>> =
        songDao.getAllSongs().map { it.map { e -> e.artist }.distinct().sorted() }

    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> =
        songDao.getSongsByAlbum(albumId).map { it.map { e -> e.toDomain() } }

    override fun getSongsByArtist(artist: String): Flow<List<Song>> =
        songDao.getSongsByArtist(artist).map { it.map { e -> e.toDomain() } }

    override fun getMostPlayed(): Flow<List<Song>> =
        songDao.getMostPlayed().map { it.map { e -> e.toDomain() } }

    override fun getRecentlyAdded(): Flow<List<Song>> =
        songDao.getRecentlyAdded().map { it.map { e -> e.toDomain() } }

    override fun getFavorites(): Flow<List<Song>> =
        songDao.getFavorites().map { it.map { e -> e.toDomain() } }

    override suspend fun toggleFavorite(songId: Long) = songDao.toggleFavorite(songId)

    override suspend fun updatePlayCount(songId: Long) = songDao.updatePlayCount(songId)

    override suspend fun triggerScan() { }
}