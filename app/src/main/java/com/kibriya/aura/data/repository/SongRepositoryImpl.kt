// MIT License
// Copyright (c) 2024 Project Aura

package com.kibriya.aura.data.repository

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kibriya.aura.data.local.dao.SongDao
import com.kibriya.aura.data.local.entity.SongEntity
import com.kibriya.aura.data.worker.MediaScanner
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

    private fun SongEntity.toDomain(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = duration,
        uri = uri,
        isFavorite = isFavorite,
        playCount = playCount,
        dateAdded = dateAdded
    )

    override fun getAllSongs(): Flow<List<Song>> =
        songDao.getAllSongs().map { list -> list.map { it.toDomain() } }

    override fun getAlbums(): Flow<List<Song>> =
        songDao.getAlbums().map { list -> list.map { it.toDomain() } }

    override fun getArtists(): Flow<List<String>> =
        songDao.getArtists()

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

    override suspend fun toggleFavorite(songId: Long) {
        songDao.toggleFavorite(songId)
    }

    override suspend fun updatePlayCount(songId: Long) {
        songDao.incrementPlayCount(songId)
    }

    override suspend fun triggerScan() {
        val request = OneTimeWorkRequestBuilder<MediaScanner>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}