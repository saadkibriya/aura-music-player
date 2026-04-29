// MIT License
// Copyright (c) 2025 Md Golam Kibriya

package com.kibriya.aura.ui.library.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibriya.aura.data.db.entity.ArtistEntity
import com.kibriya.aura.ui.theme.AuraTheme

@Composable
fun ArtistsTab(
    artists: List<ArtistEntity>,
    onArtistClick: (Long) -> Unit
) {
    if (artists.isEmpty()) {
        EmptyState(message = "No artists found.")
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(artists, key = { it.id }) { artist ->
            ArtistRow(artist = artist, onClick = { onArtistClick(artist.id) })
        }
    }
}

@Composable
private fun ArtistRow(artist: ArtistEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular avatar with first letter
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AuraTheme.colors.violet.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = artist.name.firstOrNull()?.uppercase() ?: "?",
                color = AuraTheme.colors.violet,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append("${artist.songCount} songs")
                    if (artist.albumCount > 0) append(" · ${artist.albumCount} albums")
                },
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}