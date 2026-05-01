// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.metadata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SaveState {
    object Idle    : SaveState()
    object Saving  : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

@HiltViewModel
class MetadataEditorViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    val title       = MutableStateFlow("")
    val artist      = MutableStateFlow("")
    val album       = MutableStateFlow("")
    val year        = MutableStateFlow("")
    val genre       = MutableStateFlow("")
    val albumArtUri = MutableStateFlow("")

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun loadSong(songId: Long) {
        viewModelScope.launch {
            val song = songRepository.getAllSongs().first().find { it.id == songId } ?: return@launch
            title.value       = song.title
            artist.value      = song.artist
            album.value       = song.album
            year.value        = song.year.toString()
            genre.value       = song.genre ?: ""
            albumArtUri.value = song.albumArtUri ?: ""
        }
    }

    fun updateTitle(value: String)    { title.value = value }
    fun updateArtist(value: String)   { artist.value = value }
    fun updateAlbum(value: String)    { album.value = value }
    fun updateYear(value: String)     { year.value = value }
    fun updateGenre(value: String)    { genre.value = value }
    fun updateArtUri(value: String)   { albumArtUri.value = value }

    // SongRepository has no write methods yet — save is a no-op placeholder
    fun saveMetadata() {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            _saveState.value = SaveState.Success
        }
    }
}