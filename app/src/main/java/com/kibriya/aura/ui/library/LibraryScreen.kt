// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.domain.model.Song
import com.kibriya.aura.ui.library.tabs.AlbumsTab
import com.kibriya.aura.ui.library.tabs.ArtistsTab
import com.kibriya.aura.ui.library.tabs.FoldersTab
import com.kibriya.aura.ui.library.tabs.SongsTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSongClick: (Song) -> Unit = {},
    onAlbumClick: (Long) -> Unit = {},
    onArtistClick: (String) -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val isScanning by viewModel.isScanning.collectAsState()
    val tabs = listOf("Songs", "Albums", "Artists", "Folders")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Your Library", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                if (isScanning) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title, color = Color.White) }
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> SongsTab(viewModel = viewModel, onSongClick = onSongClick)
                1 -> AlbumsTab(viewModel = viewModel, onAlbumClick = onAlbumClick)
                2 -> ArtistsTab(viewModel = viewModel, onArtistClick = onArtistClick)
                3 -> FoldersTab(viewModel = viewModel, onSongClick = onSongClick)
            }
        }
    }
}