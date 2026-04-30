/*
 * MIT License
 * Copyright (c) 2024 Saad Kibriya
 */

package com.kibriya.aura.data.repository

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kibriya.aura.data.local.dao.SongDao
import com.kibriya.aura.data.local.entity.SongEntity
import com.kibriya.aura.data.scanner.MediaScanner
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
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = duration,
        filePath = filePath,
        albumArtUri = albumArtUri,
        dateAdded = dateAdded,
        playCount = playCount,
        isFavorite = isFavorite,
        rating = rating
    )

    override fun getAllSongs(): Flow<List<Song>> =
        songDao.getAllSongs().map { list -> list.map { it.toDomain() } }

    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> =
        songDao.getSongsByAlbum(albumId).map { list -> list.map { it.toDomain() } }

    override fun getSongsByArtist(artist: String): Flow<List<Song>> =
        songDao.getSongsByArtist(artist).map { list -> list.map { it.toDomain() } }

    override fun getMostPlayed(): Flow<List<Song>> =
        songDao.getMostPlayed().map { list -> list.map { it.toDomain() } }

    override fun getRecentlyAdded(): Flow<List<Song>> =
        songDao.getRecentlyAdded().map { list -> list.map { it.toDomain() } }

    override fun getFavorites(): Flow<List<Song>> =
        songDao.getFavorites().map { list -> list.map { it.toDomain() } }

    override fun getAlbums(): Flow<List<Song>> =
        getAllSongs().map { it.distinctBy { s -> s.albumId } }

    override fun getArtists(): Flow<List<String>> =
        getAllSongs().map { it.map { s -> s.artist }.distinct() }

    override suspend fun getSongById(id: Long): Song? =
        songDao.getSongById(id)?.toDomain()

    override suspend fun updatePlayCount(id: Long) =
        songDao.updatePlayCount(id)

    override suspend fun toggleFavorite(id: Long) =
        songDao.toggleFavorite(id)

    override suspend fun upsertAll(songs: List<SongEntity>) =
        songDao.upsertAll(songs)

    override suspend fun triggerScan() {
        val request = OneTimeWorkRequestBuilder<MediaScanner>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}