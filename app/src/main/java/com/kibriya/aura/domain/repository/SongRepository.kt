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

import com.kibriya.aura.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {

    /** Returns all songs on device as a Flow, updates on DB change */
    fun getAllSongs(): Flow<List<Song>>

    /** Returns distinct album names and their representative songs */
    fun getAlbums(): Flow<Map<String, List<Song>>>

    /** Returns distinct artist names and their songs */
    fun getArtists(): Flow<Map<String, List<Song>>>

    /** All songs belonging to a specific album id */
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>

    /** All songs belonging to a specific artist id */
    fun getSongsByArtist(artistId: Long): Flow<List<Song>>

    /** Songs sorted by playCount descending, limit 50 */
    fun getMostPlayed(limit: Int = 50): Flow<List<Song>>

    /** Songs sorted by dateAdded descending, limit 50 */
    fun getRecentlyAdded(limit: Int = 50): Flow<List<Song>>

    /** Songs where isFavorite == true */
    fun getFavorites(): Flow<List<Song>>

    /** Flip isFavorite for a song, returns new state */
    suspend fun toggleFavorite(songId: Long): Boolean

    /** Increment playCount and update lastPlayed for a song */
    suspend fun updatePlayCount(songId: Long)

    /** Kick off MediaScanner WorkManager task */
    suspend fun triggerScan()
}