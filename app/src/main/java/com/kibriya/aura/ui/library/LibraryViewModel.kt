/*
 * MIT License
 * Copyright (c) 2024 Md Golam Kibriya
 */

package com.kibriya.aura.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.local.preferences.UserPreferences
import com.kibriya.aura.domain.model.Song
import com.kibriya.aura.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val songs: StateFlow<List<Song>> = songRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val albums: StateFlow<List<Song>> = songRepository.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val artists: StateFlow<List<String>> = songRepository.getArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedTab: MutableStateFlow<Int> = MutableStateFlow(0)

    val isScanning: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            if (songs.value.isEmpty()) {
                isScanning.value = true
                songRepository.triggerScan()
                isScanning.value = false
            }
        }
    }

    fun selectTab(index: Int) {
        selectedTab.value = index
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            isScanning.value = true
            songRepository.triggerScan()
            isScanning.value = false
        }
    }
}