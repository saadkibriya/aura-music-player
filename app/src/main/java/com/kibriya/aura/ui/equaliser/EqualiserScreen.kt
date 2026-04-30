/*
 * MIT License
 * Copyright (c) 2024 Md Golam Kibriya
 */
package com.kibriya.aura.ui.equaliser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.domain.model.EqPreset
import com.kibriya.aura.ui.equaliser.components.EqBandSlider
import com.kibriya.aura.ui.nowplaying.components.MeshGradientBackground
import com.kibriya.aura.ui.theme.GlassCard
import com.kibriya.aura.ui.theme.glassBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualiserScreen(
    onBack: () -> Unit,
    viewModel: EqualiserViewModel = hiltViewModel()
) {
    val bandGains by viewModel.bandGains.collectAsState()
    val isEqEnabled by viewModel.isEqEnabled.collectAsState()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsState()
    val replayGainEnabled by viewModel.replayGainEnabled.collectAsState()

    val frequencies = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    val presets = EqPreset.builtIn()

    Box(modifier = Modifier.fillMaxSize()) {
        MeshGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            TopAppBar(
                title = { Text("Equaliser") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Text(
                        text = if (isEqEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Switch(
                        checked = isEqEnabled,
                        onCheckedChange = { viewModel.toggleEq() },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preset chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.applyPreset(preset) },
                        label = { Text(preset.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // EQ band sliders
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    frequencies.forEachIndexed { index, label ->
                        val gain = if (index < bandGains.size) bandGains[index] else 0f
                        EqBandSlider(
                            gainDb = gain,
                            label = label,
                            onGainChange = { newGain -> viewModel.setBandGain(index, newGain) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Crossfade and ReplayGain
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Crossfade: ${crossfadeDuration}s",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = crossfadeDuration.toFloat(),
                        onValueChange = { viewModel.setCrossfade(it.toInt()) },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "ReplayGain",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Normalize volume across tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = replayGainEnabled,
                            onCheckedChange = { viewModel.toggleReplayGain() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}