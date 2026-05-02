// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.playlists

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.data.local.entities.PlaylistEntity
import com.kibriya.aura.domain.model.Song
import com.kibriya.aura.ui.nowplaying.components.MeshGradientBackground
import com.kibriya.aura.ui.theme.AuraViolet
import com.kibriya.aura.ui.theme.GlassCard
import com.kibriya.aura.ui.theme.GlassPillButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onNavigateToNowPlaying: () -> Unit = {},
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val mostPlayed    by viewModel.mostPlayed.collectAsState()
    val recentlyAdded by viewModel.recentlyAdded.collectAsState()
    val favorites     by viewModel.favorites.collectAsState()
    val userPlaylists by viewModel.userPlaylists.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName  by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshGradientBackground(dominantColor = Color(0xFF8B5CF6))

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Playlists", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                GlassPillButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = AuraViolet, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("New", color = AuraViolet, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Smart playlists
                item {
                    Text("Smart Playlists", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SmartCard("Most Played", Icons.Default.Equalizer, AuraViolet,
                            mostPlayed.size, onNavigateToNowPlaying)
                        SmartCard("Recently Added", Icons.Default.Schedule, Color(0xFF38BDF8),
                            recentlyAdded.size, onNavigateToNowPlaying)
                        SmartCard("Favorites", Icons.Default.Favorite, Color(0xFFF472B6),
                            favorites.size, onNavigateToNowPlaying)
                    }
                }

                // User playlists header
                item {
                    Text("My Playlists", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }

                if (userPlaylists.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center) {
                            Text("No playlists yet.\nTap + to create one.",
                                color = Color.White.copy(alpha = 0.35f), fontSize = 14.sp, lineHeight = 22.sp)
                        }
                    }
                }

                items(userPlaylists, key = { it.id }) { playlist ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == EndToStart) { viewModel.deletePlaylist(playlist.id); true }
                            else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color by animateColorAsState(
                                if (dismissState.dismissDirection == EndToStart)
                                    Color(0xFFEF4444) else Color.Transparent,
                                label = "dismissColor"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, null, tint = Color.White,
                                    modifier = Modifier.padding(end = 20.dp))
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        UserPlaylistRow(playlist = playlist)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; newPlaylistName = "" },
            containerColor = Color(0xFF1A1A2E),
            title = { Text("New Playlist", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist name", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraViolet,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                GlassPillButton(onClick = {
                    if (newPlaylistName.isNotBlank()) {
                        viewModel.createPlaylist(newPlaylistName.trim())
                        showCreateDialog = false
                        newPlaylistName = ""
                    }
                }) { Text("Create", color = AuraViolet, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; newPlaylistName = "" }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}

@Composable
private fun SmartCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    count: Int,
    onClick: () -> Unit
) {
    GlassCard(modifier = Modifier.width(130.dp).clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp, maxLines = 1)
            Text("$count songs", color = Color.White.copy(alpha = 0.45f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun UserPlaylistRow(playlist: PlaylistEntity) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(AuraViolet.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QueueMusic, null, tint = AuraViolet, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(playlist.description ?: "", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}