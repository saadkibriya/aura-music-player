/*
 * MIT License
 * Copyright (c) 2024 Md Golam Kibriya
 */
package com.kibriya.aura.ui.library.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kibriya.aura.ui.library.LibraryViewModel
import com.kibriya.aura.ui.theme.GlassCard

@Composable
fun AlbumsTab(
    viewModel: LibraryViewModel,
    onAlbumClick: (Long) -> Unit
) {
    val albums by viewModel.albums.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(albums) { song ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlbumClick(song.albumId) }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = song.album,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = song.album,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}