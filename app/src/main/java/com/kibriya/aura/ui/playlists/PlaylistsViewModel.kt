// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.local.dao.PlaylistDao
import com.kibriya.aura.data.local.entities.PlaylistEntity
import com.kibriya.aura.data.local.entities.PlaylistSongCrossRef
import com.kibriya.aura.domain.model.Song
import com.kibriya.aura.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistDao: PlaylistDao
) : ViewModel() {

    private val _mostPlayed    = MutableStateFlow<List<Song>>(emptyList())
    val mostPlayed: StateFlow<List<Song>> = _mostPlayed.asStateFlow()

    private val _recentlyAdded = MutableStateFlow<List<Song>>(emptyList())
    val recentlyAdded: StateFlow<List<Song>> = _recentlyAdded.asStateFlow()

    private val _favorites     = MutableStateFlow<List<Song>>(emptyList())
    val favorites: StateFlow<List<Song>> = _favorites.asStateFlow()

    private val _userPlaylists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
    val userPlaylists: StateFlow<List<PlaylistEntity>> = _userPlaylists.asStateFlow()

    init {
        viewModelScope.launch { songRepository.getMostPlayed().collect    { _mostPlayed.value    = it } }
        viewModelScope.launch { songRepository.getRecentlyAdded().collect { _recentlyAdded.value = it } }
        viewModelScope.launch { songRepository.getFavorites().collect     { _favorites.value     = it } }
        viewModelScope.launch { playlistDao.getAllPlaylists().collect      { _userPlaylists.value = it } }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistDao.createPlaylist(PlaylistEntity(name = name))
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            playlistDao.deletePlaylist(id)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
        }
    }
}