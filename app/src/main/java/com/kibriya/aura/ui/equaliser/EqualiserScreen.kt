/*
 * MIT License
 * Copyright (c) 2025 Md Golam Kibriya
 */

package com.kibriya.aura.ui.equaliser

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.domain.model.EqPreset
import com.kibriya.aura.ui.components.GlassCard
import com.kibriya.aura.ui.nowplaying.MeshGradientBackground
import com.kibriya.aura.ui.equaliser.components.EqBandSlider

private val VioletAccent = Color(0xFF8B5CF6)
private val AmberAccent = Color(0xFFF59E0B)
private val DarkBase = Color(0xFF0A0A0F)
private val GlassSurface = Color(0x1AFFFFFF)
private val LabelColor = Color(0xFFB0A8C8)

/** Frequency labels for 10 bands */
private val bandLabels = listOf(
    "31Hz", "62Hz", "125Hz", "250Hz", "500Hz",
    "1kHz", "2kHz", "4kHz", "8kHz", "16kHz"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualiserScreen(
    onBack: () -> Unit,
    viewModel: EqualiserViewModel = hiltViewModel()
) {
    val bandGains by viewModel.bandGains.collectAsState()
    val isEqEnabled by viewModel.isEqEnabled.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val crossfade by viewModel.crossfadeDuration.collectAsState()
    val replayGain by viewModel.replayGainEnabled.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        // Full-screen animated mesh gradient
        MeshGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Equaliser",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )

                // EQ on/off toggle — glass pill with violet glow when on
                EqToggleSwitch(
                    checked = isEqEnabled,
                    onCheckedChange = { viewModel.toggleEq() }
                )
            }

            // ── Presets row ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EqPreset.ALL.forEach { preset ->
                    val isSelected = selectedPreset?.name == preset.name
                    PresetChip(
                        label = preset.name,
                        selected = isSelected,
                        onClick = { viewModel.applyPreset(preset) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── EQ bands ──────────────────────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(220.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bandGains.forEachIndexed { index, gain ->
                        EqBandSlider(
                            gainDb = gain,
                            label = bandLabels[index],
                            onGainChange = { newGain ->
                                if (isEqEnabled) viewModel.setBandGain(index, newGain)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Crossfade slider ──────────────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Crossfade",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (crossfade == 0) "Off" else "${crossfade}s",
                            color = if (crossfade > 0) VioletAccent else LabelColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Pill-style slider matching NowPlaying progress bar style
                    Slider(
                        value = crossfade.toFloat(),
                        onValueChange = { viewModel.setCrossfade(it.toInt()) },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = VioletAccent,
                            activeTrackColor = VioletAccent,
                            inactiveTrackColor = Color(0xFF2A2040)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── ReplayGain toggle ─────────────────────────────────────────────
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ReplayGain",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Normalise loudness across tracks",
                            color = LabelColor,
                            fontSize = 11.sp
                        )
                    }

                    EqToggleSwitch(
                        checked = replayGain,
                        onCheckedChange = { viewModel.toggleReplayGain() }
                    )
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

/** Glass pill toggle switch with violet glow when checked. */
@Composable
private fun EqToggleSwitch(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "toggleThumb"
    )

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if (checked) VioletAccent.copy(alpha = 0.85f)
                else Color(0xFF2A2040)
            )
            .clickable { onCheckedChange() },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = (3 + thumbOffset * 22).dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/** Horizontally scrollable preset chip. */
@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) VioletAccent.copy(alpha = 0.9f)
                else Color(0xFF1C1830)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else LabelColor,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}