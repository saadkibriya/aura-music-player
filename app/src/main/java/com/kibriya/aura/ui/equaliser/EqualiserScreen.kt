/*
 * MIT License
 * Copyright (c) 2024 Saad Kibriya
 */

package com.kibriya.aura.ui.equaliser

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kibriya.aura.ui.equaliser.components.EqBandSlider
import com.kibriya.aura.ui.nowplaying.components.MeshGradientBackground
import com.kibriya.aura.ui.theme.GlassCard

@Composable
fun EqualiserScreen(
    viewModel: EqualiserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        MeshGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text  = "Equaliser",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment   = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Enable EQ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked         = uiState.isEnabled,
                            onCheckedChange = { viewModel.setEqEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    uiState.bandGains.forEachIndexed { index, gain ->
                        EqBandSlider(
                            bandIndex = index,
                            gain      = gain,
                            enabled   = uiState.isEnabled,
                            onGainChange = { newGain ->
                                viewModel.setBandGain(index, newGain)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text  = "Crossfade",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value         = uiState.crossfadeDuration.toFloat(),
                        onValueChange = { viewModel.setCrossfadeDuration(it.toInt()) },
                        valueRange    = 0f..10f,
                        steps         = 9,
                        enabled       = uiState.isEnabled
                    )

                    Text(
                        text  = "${uiState.crossfadeDuration}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}