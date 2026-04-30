// MIT License
//
// Copyright (c) 2024 Saad Kibriya
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.kibriya.aura.ui.metadata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val msg: String) : SaveState()
}

@HiltViewModel
class MetadataEditorViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    val title = MutableStateFlow("")
    val artist = MutableStateFlow("")
    val album = MutableStateFlow("")
    val year = MutableStateFlow("")
    val genre = MutableStateFlow("")
    val albumArtUri = MutableStateFlow("")

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    private var currentSongId: Long = -1L

    fun loadSong(songId: Long) {
        currentSongId = songId
        viewModelScope.launch {
            val song = songRepository.getSongById(songId) ?: return@launch
            title.value = song.title
            artist.value = song.artist
            album.value = song.album
            year.value = song.year.toString()
            genre.value = song.genre
            albumArtUri.value = song.albumArtUri ?: ""
        }
    }

    fun saveMetadata() {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                songRepository.updateMetadata(
                    songId = currentSongId,
                    title = title.value,
                    artist = artist.value,
                    album = album.value,
                    year = year.value.toIntOrNull() ?: 0,
                    genre = genre.value,
                    albumArtUri = albumArtUri.value
                )
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Unknown error")
            }
        }
    }
}