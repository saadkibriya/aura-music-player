/*
 * MIT License
 * Copyright (c) 2024 Saad Kibriya
 */

package com.kibriya.aura.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.ui.theme.glassBackground

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isScanning  = uiState.isScanning

    val tabs = listOf("Songs", "Albums", "Artists")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassBackground()
    ) {

        Text(
            text     = "Library",
            style    = MaterialTheme.typography.headlineMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (isScanning) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color            = MaterialTheme.colorScheme.primary,
                trackColor       = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        TabRow(
            selectedTabIndex    = selectedTab,
            containerColor      = MaterialTheme.colorScheme.surface,
            contentColor        = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = { viewModel.selectedTab.value = index },
                    text     = {
                        Text(
                            text  = title,
                            color = if (selectedTab == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> SongsTab(
                    songs      = uiState.songs,
                    onSongClick = { viewModel.onSongClick(it) }
                 )
            1 -> AlbumsTab(
                    albums      = uiState.albums,
                    onAlbumClick = { viewModel.onAlbumClick(it) }
                 )
            2 -> ArtistsTab(
                    artists      = uiState.artists,
                    onArtistClick = { viewModel.onArtistClick(it) }
                 )
        }
    }
}