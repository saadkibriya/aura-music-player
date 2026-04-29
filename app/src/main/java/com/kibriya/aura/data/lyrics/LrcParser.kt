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

package com.kibriya.aura.data.lyrics

import com.kibriya.aura.domain.model.LyricLine
import java.io.File

object LrcParser {

    // Matches standard [mm:ss.xx] or [mm:ss.xxx] timestamps
    private val TIMESTAMP_REGEX = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\]""")

    // Matches word-level enhanced LRC timestamps like <mm:ss.xx>
    private val WORD_TIMESTAMP_REGEX = Regex("""<\d{2}:\d{2}\.\d{2,3}>""")

    /**
     * Parses the content of a .lrc file into a sorted list of LyricLine.
     * Handles both standard LRC and enhanced (word-level) LRC.
     * Returns empty list on malformed/empty input.
     */
    fun parse(lrcContent: String): List<LyricLine> {
        if (lrcContent.isBlank()) return emptyList()

        val lines = mutableListOf<LyricLine>()

        for (rawLine in lrcContent.lines()) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            // Find all timestamps on this line (a single text can repeat across multiple timestamps)
            val timestamps = TIMESTAMP_REGEX.findAll(trimmed)
            if (!timestamps.any()) continue

            // Strip all timestamps and word-level tags to get clean text
            val text = TIMESTAMP_REGEX.replace(trimmed, "")
                .let { WORD_TIMESTAMP_REGEX.replace(it, "") }
                .trim()

            // Skip metadata tags like [ar:], [ti:], [al:], [by:], [offset:]
            if (text.contains(":") && text.length < 40 && !text.contains(" ")) continue
            if (text.isEmpty()) continue

            for (match in TIMESTAMP_REGEX.findAll(trimmed)) {
                val minutes = match.groupValues[1].toLongOrNull() ?: continue
                val seconds = match.groupValues[2].toLongOrNull() ?: continue
                val centisRaw = match.groupValues[3]
                // Normalize to milliseconds: 2-digit = centiseconds (*10), 3-digit = milliseconds
                val millis = if (centisRaw.length == 2) {
                    centisRaw.toLongOrNull()?.times(10) ?: continue
                } else {
                    centisRaw.toLongOrNull() ?: continue
                }
                val timestampMs = minutes * 60_000L + seconds * 1_000L + millis
                lines.add(LyricLine(timestampMs = timestampMs, text = text))
            }
        }

        return lines.sortedBy { it.timestampMs }
    }

    /**
     * Given an audio file path, looks for a matching .lrc sidecar file
     * in the same directory (same base name, .lrc extension).
     * Returns file content string or null if not found/readable.
     */
    fun findLrcFile(songFilePath: String): String? {
        return try {
            val audioFile = File(songFilePath)
            val lrcFile = File(audioFile.parent ?: return null, audioFile.nameWithoutExtension + ".lrc")
            if (lrcFile.exists() && lrcFile.canRead()) lrcFile.readText(Charsets.UTF_8) else null
        } catch (e: Exception) {
            null
        }
    }
}