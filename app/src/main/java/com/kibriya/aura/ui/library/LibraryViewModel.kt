// MIT License
// Copyright (c) 2025 Md Golam Kibriya

package com.kibriya.aura.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.db.entity.AlbumEntity
import com.kibriya.aura.data.db.entity.ArtistEntity
import com.kibriya.aura.data.db.entity.SongEntity
import com.kibriya.aura.data.repository.SongRepository
import com.kibriya.aura.data.scanner.MediaScanner
import com.kibriya.aura.data.scanner.ScanState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab { SONGS, ALBUMS, ARTISTS, FOLDERS }

data class LibraryUiState(
    val songs: List<SongEntity> = emptyList(),
    val albums: List<AlbumEntity> = emptyList(),
    val artists: List<ArtistEntity> = emptyList(),
    val selectedTab: LibraryTab = LibraryTab.SONGS,
    val scanState: ScanState = ScanState.Idle,
    val searchQuery: String = ""
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val mediaScanner: MediaScanner
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val songs: StateFlow<List<SongEntity>> = songRepository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<AlbumEntity>> = songRepository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<ArtistEntity>> = songRepository.getAllArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scanState: StateFlow<ScanState> = mediaScanner.scanState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScanState.Idle)

    val debouncedQuery: StateFlow<String> = _searchQuery
        .debounce(300L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    data class SearchResults(
        val songs: List<SongEntity> = emptyList(),
        val albums: List<AlbumEntity> = emptyList(),
        val artists: List<ArtistEntity> = emptyList()
    )

    val searchResults: StateFlow<SearchResults> = debouncedQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flow { emit(SearchResults()) }
            } else {
                combine(
                    songRepository.searchSongs(query),
                    songRepository.searchAlbums(query),
                    songRepository.searchArtists(query)
                ) { songs, albums, artists ->
                    SearchResults(songs, albums, artists)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    init {
        viewModelScope.launch {
            songs.collect { songList ->
                if (songList.isEmpty() && scanState.value == ScanState.Idle) {
                    triggerScan()
                }
            }
        }
    }

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun triggerScan() {
        viewModelScope.launch {
            mediaScanner.scan()
        }
    }
}