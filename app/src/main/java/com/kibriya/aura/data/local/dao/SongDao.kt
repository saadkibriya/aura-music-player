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
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kibriya.aura.data.local.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE album_id = :albumId ORDER BY title ASC")
    fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY title ASC")
    fun getSongsByArtist(artist: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY play_count DESC LIMIT :limit")
    fun getMostPlayed(limit: Int = 50): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY date_added DESC LIMIT :limit")
    fun getRecentlyAdded(limit: Int = 50): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE is_favorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("UPDATE songs SET play_count = play_count + 1 WHERE id = :id")
    suspend fun updatePlayCount(id: Long)

    @Query("UPDATE songs SET is_favorite = CASE WHEN is_favorite = 0 THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("UPDATE songs SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: Long, rating: Float)

    @Upsert
    suspend fun upsertAll(songs: List<SongEntity>)

    @Upsert
    suspend fun upsert(song: SongEntity)

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    @Transaction
    @Query("SELECT * FROM songs WHERE id IN (:ids) ORDER BY title ASC")
    fun getSongsByIds(ids: List<Long>): Flow<List<SongEntity>>
}