/*
 * MIT License
 * Copyright (c) 2024 Saad Kibriya
 */

package com.kibriya.aura.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.kibriya.aura.ui.library.tabs.AlbumsTab
import com.kibriya.aura.ui.library.tabs.ArtistsTab
import com.kibriya.aura.ui.library.tabs.FoldersTab
import com.kibriya.aura.ui.library.tabs.SongsTab
import com.kibriya.aura.ui.theme.glassBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onSongClick: (Long) -> Unit = {},
    onAlbumClick: (Long) -> Unit = {},
    onArtistClick: (String) -> Unit = {}
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val tabs = listOf("Songs", "Albums", "Artists", "Folders")
    val pagerState = rememberPagerState(initialPage = selectedTab)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Scanning indicator
            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF8B5CF6)
                )
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassBackground(),
                containerColor = Color.Transparent,
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            viewModel.selectTab(index)
                            CoroutineScope(Dispatchers.Main).launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    )
                }
            }

            // Pager
            HorizontalPager(
                count = tabs.size,
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> SongsTab(
                        viewModel = viewModel,
                        onSongClick = onSongClick
                    )
                    1 -> AlbumsTab(
                        viewModel = viewModel,
                        onAlbumClick = onAlbumClick
                    )
                    2 -> ArtistsTab(
                        viewModel = viewModel,
                        onArtistClick = onArtistClick
                    )
                    3 -> FoldersTab(
                        viewModel = viewModel,
                        onSongClick = onSongClick
                    )
                }
            }

            // Mini Now-Playing Bar
            MiniNowPlayingBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassBackground()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun MiniNowPlayingBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Now Playing",
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}