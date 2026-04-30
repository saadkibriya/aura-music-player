
/*
 * MIT License
 * Copyright (c) 2024 Md Golam Kibriya
 */
package com.kibriya.aura.ui.equaliser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kibriya.aura.domain.model.EqPreset
import com.kibriya.aura.ui.equaliser.components.EqBandSlider
import com.kibriya.aura.ui.nowplaying.components.MeshGradientBackground
import com.kibriya.aura.ui.theme.GlassCard

@Composable
fun EqualiserScreen(
    navController: NavController,
    viewModel: EqualiserViewModel = hiltViewModel()
) {
    val bands by viewModel.bands.collectAsState()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsState()
    val replayGainEnabled by viewModel.replayGainEnabled.collectAsState()

    val labels = listOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")

    val presetData = listOf(
        "Flat"        to List(10) { 0f },
        "Bass Boost"  to listOf(6f, 5f, 4f, 2f, 0f, 0f, 0f, 0f, 0f, 0f),
        "Treble Boost" to listOf(0f, 0f, 0f, 0f, 0f, 2f, 3f, 4f, 5f, 6f),
        "Vocal"       to listOf(-2f, -1f, 0f, 2f, 4f, 4f, 3f, 1f, 0f, -1f),
        "Rock"        to listOf(5f, 4f, 2f, 0f, -1f, 0f, 2f, 4f, 5f, 5f),
        "Electronic"  to listOf(5f, 4f, 1f, 0f, -2f, 0f, 1f, 3f, 4f, 5f),
        "Classical"   to listOf(0f, 0f, 0f, 0f, 0f, 0f, -2f, -3f, -3f, -4f),
        "Hip-Hop"     to listOf(5f, 4f, 1f, 3f, -1f, 0f, 1f, 2f, 2f, 3f)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MeshGradientBackground(dominantColor = Color(0xFF8B5CF6))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("← Back", color = Color(0xFF8B5CF6))
                }
                Spacer(Modifier.weight(1f))
                Text("Equaliser", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // Preset chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(presetData) { _, pair ->
                    val (name, gains) = pair
                    Button(
                        onClick = { viewModel.applyPreset(EqPreset(name, gains)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6).copy(alpha = 0.3f),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(name, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // EQ Band sliders
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    bands.forEachIndexed { index, gain ->
                        EqBandSlider(
                            label = labels.getOrElse(index) { "${index + 1}" },
                            gain = gain,
                            onGainChange = { viewModel.setBand(index, it) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Crossfade + ReplayGain
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Crossfade: ${crossfadeDuration}s",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = crossfadeDuration.toFloat(),
                        onValueChange = { viewModel.setCrossfade(it.toInt()) },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF8B5CF6),
                            activeTrackColor = Color(0xFF8B5CF6)
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ReplayGain", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = replayGainEnabled,
                            onCheckedChange = { viewModel.toggleReplayGain(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF8B5CF6))
                        )
                    }
                }
            }
        }
    }
}