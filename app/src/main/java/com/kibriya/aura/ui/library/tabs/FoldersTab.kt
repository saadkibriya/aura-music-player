// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.library.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kibriya.aura.domain.model.Song
import com.kibriya.aura.ui.library.LibraryViewModel

@Composable
fun FoldersTab(
    viewModel: LibraryViewModel,
    onSongClick: (Song) -> Unit
) {
    val songs by viewModel.songs.collectAsState()
    var selectedFolder by remember { mutableStateOf<String?>(null) }

    val folderMap: Map<String, List<Song>> = remember(songs) {
        songs.groupBy { song -> song.path.substringBeforeLast("/") }
    }

    if (selectedFolder == null) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(folderMap.keys.toList()) { folder ->
                val songCount = folderMap[folder]?.size ?: 0
                val folderName = folder.substringAfterLast("/")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFolder = folder }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null,
                        tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(folderName, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text("$songCount songs", color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    } else {
        val folderSongs = folderMap[selectedFolder] ?: emptyList()
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedFolder = null }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("← Back", color = Color(0xFF8B5CF6), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                Text(selectedFolder!!.substringAfterLast("/"), color = Color.White,
                    style = MaterialTheme.typography.bodyLarge)
            }
            LazyColumn {
                items(folderSongs) { song ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSongClick(song) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MusicNote, null, tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(song.title, color = Color.White,
                                style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            Text(song.artist, color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}