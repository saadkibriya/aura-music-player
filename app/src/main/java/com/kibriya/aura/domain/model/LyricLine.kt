// MIT License
// Copyright (c) 2025 Md Golam Kibriya
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

package com.kibriya.aura.domain.model

/**
 * Represents a single parsed lyric line from an LRC file.
 *
 * @param timestampMs  Playback position in milliseconds at which this line should become active.
 * @param text         Display text of the lyric line (word-level tags already stripped).
 * @param isActive     Whether this line is currently highlighted (set by LyricsViewModel).
 */
data class LyricLine(
    val timestampMs: Long,
    val text: String,
    val isActive: Boolean = false
)