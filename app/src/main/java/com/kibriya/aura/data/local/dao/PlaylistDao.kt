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

package com.kibriya.aura.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.kibriya.aura.data.local.entities.PlaylistEntity
import com.kibriya.aura.data.local.entities.PlaylistSongCrossRef
import com.kibriya.aura.data.local.entities.SongEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PlaylistSongCrossRef::class,
            parentColumn = "playlist_id",
            entityColumn = "song_id"
        )
    )
    val songs: List<SongEntity>
)

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY updated_at DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithSongs(playlistId: Long): Flow<PlaylistWithSongs?>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query(
        "DELETE FROM playlist_song_cross_ref WHERE playlist_id = :playlistId AND song_id = :songId"
    )
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query(
        "DELETE FROM playlist_song_cross_ref WHERE playlist_id = :playlistId"
    )
    suspend fun clearPlaylist(playlistId: Long)

    @Query(
        "SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlist_id = :playlistId"
    )
    suspend fun getSongCountInPlaylist(playlistId: Long): Int

    @Query(
        "SELECT EXISTS(SELECT 1 FROM playlist_song_cross_ref WHERE playlist_id = :playlistId AND song_id = :songId LIMIT 1)"
    )
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean

    @Query(
        "UPDATE playlist_song_cross_ref SET position = :position WHERE playlist_id = :playlistId AND song_id = :songId"
    )
    suspend fun updateSongPosition(playlistId: Long, songId: Long, position: Int)

    @Query(
        "UPDATE playlists SET updated_at = :timestamp WHERE id = :playlistId"
    )
    suspend fun touchPlaylist(playlistId: Long, timestamp: Long = System.currentTimeMillis())
}