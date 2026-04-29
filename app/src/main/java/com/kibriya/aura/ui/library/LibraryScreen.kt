// MIT License
// Copyright (c) 2025 Md Golam Kibriya

package com.kibriya.aura.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kibriya.aura.data.scanner.ScanState
import com.kibriya.aura.ui.library.tabs.AlbumsTab
import com.kibriya.aura.ui.library.tabs.ArtistsTab
import com.kibriya.aura.ui.library.tabs.FoldersTab
import com.kibriya.aura.ui.library.tabs.SongsTab
import com.kibriya.aura.ui.theme.AuraTheme
import com.kibriya.aura.ui.theme.glassBackground
import kotlinx.coroutines.launch

private data class TabInfo(val label: String, val icon: ImageVector)

private val libraryTabs = listOf(
    TabInfo("Songs", Icons.Rounded.LibraryMusic),
    TabInfo("Albums", Icons.Rounded.LibraryMusic),
    TabInfo("Artists", Icons.Rounded.Person),
    TabInfo("Folders", Icons.Rounded.Folder)
)

@Composable
fun LibraryScreen(
    onNavigateToNowPlaying: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToArtist: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val pagerState = rememberPagerState(pageCount = { libraryTabs.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraTheme.colors.darkBase)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            // Scan progress
            AnimatedVisibility(
                visible = scanState is ScanState.Scanning,
                enter = fadeIn(tween(300)) + slideInVertically(),
                exit = fadeOut(tween(300))
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(
                        text = if (scanState is ScanState.Scanning)
                            "Scanning... ${(scanState as ScanState.Scanning).progress}%"
                        else "Scanning...",
                        color = AuraTheme.colors.violet,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (scanState is ScanState.Scanning)
                                (scanState as ScanState.Scanning).progress / 100f
                            else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(50)),
                        color = AuraTheme.colors.violet,
                        trackColor = AuraTheme.colors.violet.copy(alpha = 0.15f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                            .height(3.dp)
                            .clip(RoundedCornerShape(50)),
                        color = AuraTheme.colors.violet
                    )
                },
                divider = {}
            ) {
                libraryTabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                            viewModel.selectTab(LibraryTab.entries[index])
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .then(
                                if (pagerState.currentPage == index)
                                    Modifier.glassBackground()
                                else Modifier
                            )
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = tab.label,
                            color = if (pagerState.currentPage == index) Color.White
                            else Color.White.copy(alpha = 0.45f),
                            fontSize = 14.sp,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.SemiBold
                            else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> SongsTab(
                        songs = songs,
                        onSongClick = { index ->
                            onNavigateToNowPlaying()
                        }
                    )
                    1 -> AlbumsTab(
                        albums = albums,
                        onAlbumClick = onNavigateToAlbum
                    )
                    2 -> ArtistsTab(
                        artists = artists,
                        onArtistClick = onNavigateToArtist
                    )
                    3 -> FoldersTab(songs = songs)
                }
            }

            // Mini Now Playing bar placeholder — connected to PlayerRepository in full impl
            MiniNowPlayingBar(onTap = onNavigateToNowPlaying)
        }
    }
}

@Composable
private fun MiniNowPlayingBar(onTap: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .glassBackground()
            .clickable { onTap() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuraTheme.colors.violet.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LibraryMusic,
                    contentDescription = null,
                    tint = AuraTheme.colors.violet,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Now Playing",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tap to open",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { /* toggle play/pause via PlayerRepository */ }) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = AuraTheme.colors.violet
                )
            }
        }
    }
}