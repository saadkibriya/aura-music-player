// MIT License
// Copyright (c) 2025 Md Golam Kibriya (Saad)
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.

package com.kibriya.aura.ui.library.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kibriya.aura.data.model.Song
import com.kibriya.aura.ui.components.glassBackground
import com.kibriya.aura.ui.library.LibraryViewModel
import com.kibriya.aura.ui.theme.AuraAmber
import com.kibriya.aura.ui.theme.AuraViolet

// ─────────────────────────────────────────────────────────────────────────────
// Data model for folder tree nodes
// ─────────────────────────────────────────────────────────────────────────────

data class FolderNode(
    val name: String,
    val fullPath: String,
    val songs: List<Song>,
    val children: List<FolderNode> = emptyList()
) {
    val totalSongCount: Int
        get() = songs.size + children.sumOf { it.totalSongCount }
}

// ─────────────────────────────────────────────────────────────────────────────
// Build folder tree from flat song list
// ─────────────────────────────────────────────────────────────────────────────

fun buildFolderTree(songs: List<Song>): List<FolderNode> {
    // Group songs by their parent directory path
    val grouped = songs.groupBy { song ->
        val path = song.data
        path.substringBeforeLast("/", "/")
    }

    // Find top-level folders (those not nested under another grouped folder)
    val allPaths = grouped.keys.toSet()
    val topLevelPaths = allPaths.filter { path ->
        allPaths.none { other -> other != path && path.startsWith("$other/") }
    }

    fun buildNode(path: String): FolderNode {
        val directSongs = grouped[path] ?: emptyList()
        val childPaths = allPaths.filter { it.startsWith("$path/") && it != path }
        val directChildPaths = childPaths.filter { child ->
            childPaths.none { other ->
                other != child && child.startsWith("$other/")
            }
        }
        return FolderNode(
            name = path.substringAfterLast("/").ifEmpty { path },
            fullPath = path,
            songs = directSongs,
            children = directChildPaths.map { buildNode(it) }.sortedBy { it.name }
        )
    }

    return topLevelPaths.map { buildNode(it) }.sortedBy { it.name }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main FoldersTab Composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FoldersTab(
    viewModel: LibraryViewModel,
    onSongClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsState()
    val folderTree = remember(songs) { buildFolderTree(songs) }

    // Breadcrumb state: list of (name, fullPath)
    var breadcrumbs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var expandedPaths by remember { mutableStateOf<Set<String>>(emptySet()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Breadcrumb Row ────────────────────────────────────────────────────
        if (breadcrumbs.isNotEmpty()) {
            BreadcrumbRow(
                breadcrumbs = breadcrumbs,
                onSegmentClick = { index ->
                    breadcrumbs = breadcrumbs.take(index + 1)
                },
                onRootClick = {
                    breadcrumbs = emptyList()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Folder List ───────────────────────────────────────────────────────
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Determine which nodes to show based on breadcrumb depth
            val currentNodes = if (breadcrumbs.isEmpty()) {
                folderTree
            } else {
                // Navigate to the node at current breadcrumb path
                fun findNode(nodes: List<FolderNode>, path: String): FolderNode? {
                    for (node in nodes) {
                        if (node.fullPath == path) return node
                        val found = findNode(node.children, path)
                        if (found != null) return found
                    }
                    return null
                }
                val currentPath = breadcrumbs.last().second
                findNode(folderTree, currentPath)?.children ?: emptyList()
            }

            items(currentNodes, key = { it.fullPath }) { folder ->
                FolderRow(
                    folder = folder,
                    isExpanded = folder.fullPath in expandedPaths,
                    onFolderClick = {
                        if (folder.children.isNotEmpty()) {
                            // Navigate deeper via breadcrumb
                            breadcrumbs = breadcrumbs + (folder.name to folder.fullPath)
                        } else {
                            // Toggle inline expansion to show songs
                            expandedPaths = if (folder.fullPath in expandedPaths) {
                                expandedPaths - folder.fullPath
                            } else {
                                expandedPaths + folder.fullPath
                            }
                        }
                    },
                    onSongClick = onSongClick
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Breadcrumb Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BreadcrumbRow(
    breadcrumbs: List<Pair<String, String>>,
    onSegmentClick: (Int) -> Unit,
    onRootClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .glassBackground(cornerRadius = 12.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Root segment
        Text(
            text = "📁 Root",
            color = AuraViolet,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onRootClick() }
        )

        breadcrumbs.forEachIndexed { index, (name, _) ->
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = name,
                color = if (index == breadcrumbs.lastIndex) Color.White else AuraViolet,
                fontSize = 12.sp,
                fontWeight = if (index == breadcrumbs.lastIndex) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.clickable { onSegmentClick(index) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Individual Folder Row with inline song expansion
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FolderRow(
    folder: FolderNode,
    isExpanded: Boolean,
    onFolderClick: () -> Unit,
    onSongClick: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassBackground(cornerRadius = 16.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Folder header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onFolderClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                tint = AuraAmber,
                modifier = Modifier.size(26.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${folder.totalSongCount} song${if (folder.totalSongCount != 1) "s" else ""}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            // Indicator
            if (folder.children.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate into folder",
                    tint = AuraViolet.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Inline song list (only for leaf folders with songs)
        AnimatedVisibility(
            visible = isExpanded && folder.songs.isNotEmpty(),
            enter = expandVertically(animationSpec = tween(280)),
            exit = shrinkVertically(animationSpec = tween(200))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                folder.songs.forEach { song ->
                    SongRowInFolder(song = song, onClick = { onSongClick(song) })
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Compact song row inside expanded folder
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SongRowInFolder(
    song: Song,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = AuraViolet.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatDuration(song.duration),
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 11.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utility
// ─────────────────────────────────────────────────────────────────────────────

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}