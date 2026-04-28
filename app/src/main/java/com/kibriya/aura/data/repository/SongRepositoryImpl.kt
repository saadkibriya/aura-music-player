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
    @ApplicationContext private val context: Context,
    private val songDao: SongDao
) : SongRepository {

    // ── Mappers ──────────────────────────────────────────────────────────────

    private fun SongEntity.toDomain(): Song = Song(
        id           = id,
        title        = title,
        artist       = artist,
        album        = album,
        albumId      = albumId,
        artistId     = artistId,
        duration     = duration,
        path         = path,
        uri          = uri,
        albumArtUri  = albumArtUri,
        trackNumber  = trackNumber,
        year         = year,
        genre        = genre,
        bitrate      = bitrate,
        sampleRate   = sampleRate,
        size         = size,
        dateAdded    = dateAdded,
        dateModified = dateModified,
        playCount    = playCount,
        isFavorite   = isFavorite,
        lastPlayed   = lastPlayed
    )

    // ── SongRepository ───────────────────────────────────────────────────────

    override fun getAllSongs(): Flow<List<Song>> =
        songDao.getAllSongs().map { list -> list.map { it.toDomain() } }

    override fun getAlbums(): Flow<Map<String, List<Song>>> =
        getAllSongs().map { songs ->
            songs.groupBy { it.album }
        }

    override fun getArtists(): Flow<Map<String, List<Song>>> =
        getAllSongs().map { songs ->
            songs.groupBy { it.artist }
        }

    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> =
        songDao.getSongsByAlbum(albumId).map { list -> list.map { it.toDomain() } }

    override fun getSongsByArtist(artistId: Long): Flow<List<Song>> =
        songDao.getSongsByArtist(artistId).map { list -> list.map { it.toDomain() } }

    override fun getMostPlayed(limit: Int): Flow<List<Song>> =
        songDao.getMostPlayed(limit).map { list -> list.map { it.toDomain() } }

    override fun getRecentlyAdded(limit: Int): Flow<List<Song>> =
        songDao.getRecentlyAdded(limit).map { list -> list.map { it.toDomain() } }

    override fun getFavorites(): Flow<List<Song>> =
        songDao.getFavorites().map { list -> list.map { it.toDomain() } }

    override suspend fun toggleFavorite(songId: Long): Boolean {
        val entity = songDao.getSongById(songId) ?: return false
        val newState = !entity.isFavorite
        songDao.updateFavorite(songId, newState)
        return newState
    }

    override suspend fun updatePlayCount(songId: Long) {
        songDao.incrementPlayCount(songId, System.currentTimeMillis())
    }

    override suspend fun triggerScan() {
        val request = OneTimeWorkRequestBuilder<MediaScanner>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}