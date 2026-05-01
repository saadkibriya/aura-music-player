// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.equaliser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.domain.model.EqPreset
import com.kibriya.aura.ui.equaliser.components.EqBandSlider
import com.kibriya.aura.ui.theme.GlassCard

private val bandLabels = listOf("60Hz", "230Hz", "910Hz", "4kHz", "14kHz")

private val builtInPresets = listOf(
    EqPreset("Flat",       listOf(0f, 0f, 0f, 0f, 0f)),
    EqPreset("Bass Boost", listOf(6f, 4f, 0f, 0f, 0f)),
    EqPreset("Treble",     listOf(0f, 0f, 0f, 4f, 6f)),
    EqPreset("Vocal",      listOf(-2f, 0f, 4f, 3f, 0f)),
    EqPreset("Electronic", listOf(4f, 2f, -1f, 2f, 4f))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualiserScreen(
    onBack: () -> Unit,
    viewModel: EqualiserViewModel = hiltViewModel()
) {
    val bandGains by viewModel.bandGains.collectAsState()
    val isEqEnabled by viewModel.isEqEnabled.collectAsState()
    val crossfade by viewModel.crossfadeDuration.collectAsState()
    val replayGain by viewModel.replayGainEnabled.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Equaliser", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // EQ toggle
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Equaliser", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isEqEnabled,
                        onCheckedChange = { viewModel.toggleEq() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF8B5CF6))
                    )
                }
            }

            // Band sliders
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val gains = if (bandGains.size >= bandLabels.size)
                        bandGains
                    else
                        List(bandLabels.size) { i -> bandGains.getOrElse(i) { 0f } }

                    gains.forEachIndexed { index, gain ->
                        EqBandSlider(
                            gainDb = gain,
                            label = bandLabels.getOrElse(index) { "${index + 1}" },
                            onGainChange = { newGain -> viewModel.setBandGain(index, newGain) }
                        )
                    }
                }
            }

            // Presets
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Presets", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(10.dp))
                    builtInPresets.forEach { preset ->
                        TextButton(onClick = { viewModel.applyPreset(preset) }) {
                            Text(preset.name, color = Color(0xFF8B5CF6))
                        }
                    }
                }
            }

            // Crossfade
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Crossfade", color = Color.White, fontSize = 16.sp)
                        Text("${crossfade}s", color = Color(0xFF8B5CF6), fontSize = 14.sp)
                    }
                    Slider(
                        value = crossfade.toFloat(),
                        onValueChange = { viewModel.setCrossfade(it.toInt()) },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF8B5CF6), activeTrackColor = Color(0xFF8B5CF6))
                    )
                }
            }

            // Replay Gain
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Replay Gain", color = Color.White, fontSize = 16.sp)
                    Switch(
                        checked = replayGain,
                        onCheckedChange = { viewModel.toggleReplayGain() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF8B5CF6))
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}