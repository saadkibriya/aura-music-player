/*
 * MIT License
 * Copyright (c) 2025 Md Golam Kibriya
 */

package com.kibriya.aura.domain.model

/**
 * EqPreset — named snapshot of 10 band gains (dB, -12..+12).
 * Index order matches AuraAudioProcessor band order:
 * 31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz
 */
data class EqPreset(
    val name: String,
    val gains: List<Float>   // exactly 10 values
) {
    init {
        require(gains.size == 10) { "EqPreset requires exactly 10 band gains" }
    }

    companion object {
        val FLAT = EqPreset(
            name = "Flat",
            gains = List(10) { 0f }
        )

        val BASS_BOOST = EqPreset(
            name = "Bass Boost",
            gains = listOf(8f, 7f, 6f, 3f, 1f, 0f, 0f, 0f, 0f, 0f)
        )

        val TREBLE_BOOST = EqPreset(
            name = "Treble Boost",
            gains = listOf(0f, 0f, 0f, 0f, 0f, 1f, 3f, 5f, 7f, 8f)
        )

        val VOCAL = EqPreset(
            name = "Vocal",
            gains = listOf(-2f, -2f, 0f, 2f, 5f, 6f, 5f, 3f, 1f, 0f)
        )

        val ROCK = EqPreset(
            name = "Rock",
            gains = listOf(5f, 4f, 3f, 1f, -1f, -1f, 1f, 3f, 4f, 5f)
        )

        val ELECTRONIC = EqPreset(
            name = "Electronic",
            gains = listOf(6f, 5f, 1f, -2f, -3f, 1f, 3f, 4f, 5f, 6f)
        )

        val CLASSICAL = EqPreset(
            name = "Classical",
            gains = listOf(4f, 3f, 2f, 1f, 0f, 0f, -1f, -1f, 2f, 3f)
        )

        val HIP_HOP = EqPreset(
            name = "Hip-Hop",
            gains = listOf(7f, 6f, 3f, 4f, 1f, -1f, -1f, 2f, 3f, 4f)
        )

        /** All built-in presets in display order. */
        val ALL: List<EqPreset> = listOf(
            FLAT, BASS_BOOST, TREBLE_BOOST, VOCAL,
            ROCK, ELECTRONIC, CLASSICAL, HIP_HOP
        )
    }
}