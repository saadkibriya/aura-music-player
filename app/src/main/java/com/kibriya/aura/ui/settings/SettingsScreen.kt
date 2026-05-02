// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.BuildConfig
import com.kibriya.aura.ui.nowplaying.components.MeshGradientBackground
import com.kibriya.aura.ui.theme.AuraAmber
import com.kibriya.aura.ui.theme.AuraViolet
import com.kibriya.aura.ui.theme.GlassCard
import com.kibriya.aura.ui.theme.GlassPillButton

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsState()
    val replayGainEnabled by viewModel.replayGainEnabled.collectAsState()
    val gaplessEnabled    by viewModel.gaplessEnabled.collectAsState()
    val sleepTimerMs      by viewModel.sleepTimerMs.collectAsState()
    val accentColor       by viewModel.accentColor.collectAsState()
    val uriHandler        = LocalUriHandler.current

    Box(modifier = Modifier.fillMaxSize()) {
        MeshGradientBackground(dominantColor = Color(0xFF8B5CF6))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            // Playback
            SettingsSection(title = "Playback", icon = Icons.Default.Tune) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Crossfade", color = Color.White, fontSize = 14.sp)
                            Text("${crossfadeDuration}s", color = AuraViolet, fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold)
                        }
                        Slider(
                            value = crossfadeDuration.toFloat(),
                            onValueChange = { viewModel.setCrossfade(it.toInt()) },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = AuraViolet,
                                activeTrackColor = AuraViolet,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )
                    }
                    SettingsToggleRow("ReplayGain", replayGainEnabled) { viewModel.toggleReplayGain() }
                    SettingsToggleRow("Gapless Playback", gaplessEnabled) { viewModel.toggleGapless() }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sleep Timer
            SettingsSection(title = "Sleep Timer", icon = Icons.Default.Bedtime) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Duration", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 30, 45, 60).forEach { minutes ->
                            val ms = minutes * 60_000L
                            val selected = sleepTimerMs == ms
                            GlassPillButton(
                                onClick = { viewModel.setSleepTimer(ms) },
                                modifier = Modifier.height(36.dp),
                                glowColor = if (selected) AuraViolet else Color.Transparent
                            ) {
                                Text(
                                    "${minutes}m",
                                    color = if (selected) AuraViolet else Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Appearance
            SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Accent Color", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(
                            "violet" to Color(0xFF8B5CF6),
                            "amber"  to Color(0xFFF59E0B),
                            "teal"   to Color(0xFF14B8A6)
                        ).forEach { (key, color) ->
                            val selected = accentColor == key
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selected) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccentColor(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) Icon(Icons.Default.Check, null,
                                    tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Library
            SettingsSection(title = "Library", icon = Icons.Default.LibraryMusic) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassPillButton(
                        onClick = { viewModel.triggerRescan() },
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, tint = AuraViolet, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Rescan Library", color = AuraViolet, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // About
            SettingsSection(title = "About", icon = Icons.Default.Info) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AboutRow("Version", BuildConfig.VERSION_NAME)
                    AboutRow("Developer", "Md Golam Kibriya")
                    AboutRow("License", "MIT")
                    GlassPillButton(
                        onClick = { uriHandler.openUri("https://github.com/saadkibriya/aura-music-player") },
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Icon(Icons.Default.Code, null, tint = AuraViolet, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View on GitHub", color = AuraViolet, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Icon(icon, null, tint = AuraViolet, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AuraViolet,
                uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.12f)
            )
        )
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}