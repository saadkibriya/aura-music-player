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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.kibriya.aura.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kibriya.aura.data.local.dao.SongDao
import com.kibriya.aura.data.local.entities.AlbumEntity
import com.kibriya.aura.data.local.entities.ArtistEntity
import com.kibriya.aura.data.local.entities.SongEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ──────────────────────────────────────────────────────────────────────────────
// Scan progress model
// ──────────────────────────────────────────────────────────────────────────────

sealed class ScanState {
    object Idle : ScanState()
    data class Scanning(val scanned: Int, val total: Int) : ScanState()
    data class Completed(val songCount: Int, val albumCount: Int, val artistCount: Int) : ScanState()
    data class Error(val message: String) : ScanState()
}

// ──────────────────────────────────────────────────────────────────────────────
// Scanner singleton — holds the StateFlow and triggers WorkManager
// ──────────────────────────────────────────────────────────────────────────────

@Singleton
class MediaScannerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    fun updateState(state: ScanState) {
        _scanState.value = state
    }

    fun startScan() {
        val request = OneTimeWorkRequestBuilder<MediaScanWorker>()
            .setConstraints(Constraints.NONE)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            MediaScanWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancelScan() {
        WorkManager.getInstance(context).cancelUniqueWork(MediaScanWorker.WORK_NAME)
        _scanState.value = ScanState.Idle
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// WorkManager Worker
// ──────────────────────────────────────────────────────────────────────────────

@HiltWorker
class MediaScanWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val songDao: SongDao,
    private val scannerManager: MediaScannerManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "aura_media_scan"
        const val KEY_SONG_COUNT = "song_count"
        const val KEY_ALBUM_COUNT = "album_count"
        const val KEY_ARTIST_COUNT = "artist_count"
        private const val MIN_DURATION_MS = 30_000L // 30 seconds
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            scannerManager.updateState(ScanState.Scanning(0, 0))

            val contentResolver = applicationContext.contentResolver
            val rawSongs = queryMediaStore(contentResolver)

            if (rawSongs.isEmpty()) {
                scannerManager.updateState(ScanState.Completed(0, 0, 0))
                return@withContext Result.success(
                    workDataOf(KEY_SONG_COUNT to 0, KEY_ALBUM_COUNT to 0, KEY_ARTIST_COUNT to 0)
                )
            }

            val total = rawSongs.size
            val songs = mutableListOf<SongEntity>()
            val albumMap = mutableMapOf<Long, AlbumEntity>()
            val artistMap = mutableMapOf<String, ArtistEntity>()

            rawSongs.forEachIndexed { index, raw ->
                scannerManager.updateState(ScanState.Scanning(index + 1, total))

                songs.add(raw.toSongEntity())

                // Build album map (accumulate song counts)
                val existing = albumMap[raw.albumId]
                if (existing == null) {
                    albumMap[raw.albumId] = AlbumEntity(
                        id = raw.albumId,
                        title = raw.album,
                        artist = raw.artist,
                        albumArtUri = raw.albumArtUri,
                        songCount = 1,
                        year = raw.year
                    )
                } else {
                    albumMap[raw.albumId] = existing.copy(songCount = existing.songCount + 1)
                }

                // Build artist map
                val artistKey = raw.artist
                val existingArtist = artistMap[artistKey]
                if (existingArtist == null) {
                    artistMap[artistKey] = ArtistEntity(
                        name = artistKey,
                        albumCount = 1,
                        songCount = 1
                    )
                } else {
                    val newAlbumCount = if (albumMap.values.any {
                            it.artist == artistKey
                        }
                    ) albumMap.values.count { it.artist == artistKey }
                    else existingArtist.albumCount

                    artistMap[artistKey] = existingArtist.copy(
                        songCount = existingArtist.songCount + 1,
                        albumCount = newAlbumCount
                    )
                }
            }

            // Persist to Room
            songDao.upsertAll(songs)

            val songCount = songs.size
            val albumCount = albumMap.size
            val artistCount = artistMap.size

            scannerManager.updateState(
                ScanState.Completed(songCount, albumCount, artistCount)
            )

            Result.success(
                workDataOf(
                    KEY_SONG_COUNT to songCount,
                    KEY_ALBUM_COUNT to albumCount,
                    KEY_ARTIST_COUNT to artistCount
                )
            )
        } catch (e: Exception) {
            scannerManager.updateState(ScanState.Error(e.message ?: "Unknown scan error"))
            Result.failure(workDataOf("error" to e.message))
        }
    }

    // ── MediaStore query ──────────────────────────────────────────────────────

    private fun queryMediaStore(contentResolver: ContentResolver): List<RawSongData> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= $MIN_DURATION_MS"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val results = mutableListOf<RawSongData>()

        contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                ).toString()

                results.add(
                    RawSongData(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown Artist",
                        album = cursor.getString(albumCol) ?: "Unknown Album",
                        albumId = albumId,
                        duration = cursor.getLong(durationCol),
                        filePath = cursor.getString(dataCol) ?: "",
                        albumArtUri = albumArtUri,
                        dateAdded = cursor.getLong(dateAddedCol),
                        year = cursor.getInt(yearCol).takeIf { it > 0 }
                    )
                )
            }
        }

        return results
    }
}

// ── Internal raw data model (not persisted directly) ─────────────────────────

private data class RawSongData(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val filePath: String,
    val albumArtUri: String,
    val dateAdded: Long,
    val year: Int?
) {
    fun toSongEntity() = SongEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = duration,
        filePath = filePath,
        albumArtUri = albumArtUri,
        dateAdded = dateAdded
    )
}