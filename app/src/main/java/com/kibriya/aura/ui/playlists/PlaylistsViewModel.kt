// MIT License — Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.db.dao.PlaylistDao
import com.kibriya.aura.data.db.dao.SongDao
import com.kibriya.aura.data.db.entity.PlaylistEntity
import com.kibriya.aura.data.db.entity.PlaylistSongCrossRef
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SmartPlaylist(
    val id: String,
    val name: String,
    val icon: String,        // Material icon name
    val songCount: Int,
    val songIds: List<Long>
)

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) : ViewModel() {

    val smartPlaylists: StateFlow<List<SmartPlaylist>> = combine(
        songDao.getMostPlayed(25),
        songDao.getRecentlyAdded(25),
        songDao.getTopRated(25),
        songDao.getFavorites()
    ) { mostPlayed, recentlyAdded, topRated, favorites ->
        listOf(
            SmartPlaylist(
                id = "smart_most_played",
                name = "Most Played",
                icon = "equalizer",
                songCount = mostPlayed.size,
                songIds = mostPlayed.map { it.id }
            ),
            SmartPlaylist(
                id = "smart_recently_added",
                name = "Recently Added",
                icon = "schedule",
                songCount = recentlyAdded.size,
                songIds = recentlyAdded.map { it.id }
            ),
            SmartPlaylist(
                id = "smart_top_rated",
                name = "Top Rated",
                icon = "star",
                songCount = topRated.size,
                songIds = topRated.map { it.id }
            ),
            SmartPlaylist(
                id = "smart_favorites",
                name = "Favorites",
                icon = "favorite",
                songCount = favorites.size,
                songIds = favorites.map { it.id }
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPlaylists: StateFlow<List<PlaylistEntity>> =
        playlistDao.getAllPlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistDao.insertPlaylist(PlaylistEntity(name = name, createdAt = System.currentTimeMillis()))
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            playlistDao.deletePlaylistById(id)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistDao.removeSongFromPlaylist(playlistId, songId)
        }
    }
}