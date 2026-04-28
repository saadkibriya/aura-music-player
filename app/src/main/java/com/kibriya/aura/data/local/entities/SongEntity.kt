/*
 * MIT License
 * Copyright (c) 2025 Md Golam Kibriya
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 */

package com.kibriya.aura.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["file_path"], unique = true),
        Index(value = ["album_id"]),
        Index(value = ["artist"]),
        Index(value = ["date_added"]),
        Index(value = ["play_count"]),
        Index(value = ["is_favorite"])
    ]
)
data class SongEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "artist")
    val artist: String,

    @ColumnInfo(name = "album")
    val album: String,

    @ColumnInfo(name = "album_id")
    val albumId: Long,

    @ColumnInfo(name = "duration")
    val duration: Long, // milliseconds

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "album_art_uri")
    val albumArtUri: String?,

    @ColumnInfo(name = "date_added")
    val dateAdded: Long, // epoch seconds

    @ColumnInfo(name = "play_count", defaultValue = "0")
    val playCount: Int = 0,

    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "rating", defaultValue = "0")
    val rating: Float = 0f // 0.0 – 5.0
)