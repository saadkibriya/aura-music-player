// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.metadata

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kibriya.aura.ui.nowplaying.components.MeshGradientBackground
import com.kibriya.aura.ui.theme.GlassCard
import com.kibriya.aura.ui.theme.GlassPillButton

@Composable
fun MetadataEditorScreen(
    songId: Long,
    navController: NavController,
    viewModel: MetadataEditorViewModel = hiltViewModel()
) {
    LaunchedEffect(songId) { viewModel.loadSong(songId) }

    val title     by viewModel.title.collectAsState()
    val artist    by viewModel.artist.collectAsState()
    val album     by viewModel.album.collectAsState()
    val year      by viewModel.year.collectAsState()
    val genre     by viewModel.genre.collectAsState()
    val artUri    by viewModel.albumArtUri.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.updateArtUri(it.toString()) } }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) navController.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshGradientBackground(dominantColor = Color(0xFF8B5CF6))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text("Edit Metadata", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (artUri.isNotEmpty()) {
                            AsyncImage(
                                model = artUri,
                                contentDescription = "Album art",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Tap to change artwork", fontSize = 12.sp, color = Color.White.copy(alpha = 0.45f))
                }
            }

            Spacer(Modifier.height(12.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MetaTextField("Title",  title,  viewModel::updateTitle)
                    MetaTextField("Artist", artist, viewModel::updateArtist)
                    MetaTextField("Album",  album,  viewModel::updateAlbum)
                    MetaTextField("Year",   year,   viewModel::updateYear)
                    MetaTextField("Genre",  genre,  viewModel::updateGenre)
                }
            }

            Spacer(Modifier.height(20.dp))

            if (saveState is SaveState.Error) {
                Text(
                    text = "Save failed: ${(saveState as SaveState.Error).message}",
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
                GlassPillButton(
                    onClick = { viewModel.saveMetadata() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    glowColor = Color(0xFF8B5CF6),
                    enabled = saveState !is SaveState.Saving
                ) {
                    if (saveState is SaveState.Saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF8B5CF6),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Save, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save", color = Color(0xFF8B5CF6), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MetaTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Color(0xFF8B5CF6),
            unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
            focusedTextColor     = Color.White,
            unfocusedTextColor   = Color.White,
            focusedLabelColor    = Color(0xFF8B5CF6),
            unfocusedLabelColor  = Color.White.copy(alpha = 0.45f),
            cursorColor          = Color(0xFF8B5CF6)
        )
    )
}