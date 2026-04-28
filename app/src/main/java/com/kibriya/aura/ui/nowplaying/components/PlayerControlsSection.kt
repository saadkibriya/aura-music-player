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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 */

package com.kibriya.aura.ui.nowplaying.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kibriya.aura.domain.model.PlayerState
import com.kibriya.aura.ui.theme.AuraViolet
import com.kibriya.aura.ui.theme.GlassCard
import com.kibriya.aura.ui.theme.auraGlow
import com.kibriya.aura.ui.theme.SquircleShape

@Composable
fun PlayerControlsSection(
    isPlaying: Boolean,
    isShuffled: Boolean,
    repeatMode: Int,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main playback row: Previous | Play/Pause | Next
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SpringIconButton(
                icon = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                onClick = onSkipPrevious,
                size = 56.dp,
                iconSize = 28.dp,
                glowColor = Color.Transparent
            )

            SpringIconButton(
                icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                onClick = if (isPlaying) onPause else onPlay,
                size = 72.dp,
                iconSize = 38.dp,
                glowColor = if (isPlaying) AuraViolet else Color.Transparent,
                isPrimary = true
            )

            SpringIconButton(
                icon = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                onClick = onSkipNext,
                size = 56.dp,
                iconSize = 28.dp,
                glowColor = Color.Transparent
            )
        }

        // Shuffle + Repeat row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            val shuffleInteraction = remember { MutableInteractionSource() }
            val shufflePressed by shuffleInteraction.collectIsPressedAsState()
            val shuffleScale by animateFloatAsState(
                targetValue = if (shufflePressed) 0.88f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "shuffleScale"
            )
            IconButton(
                onClick = onToggleShuffle,
                interactionSource = shuffleInteraction,
                modifier = Modifier.scale(shuffleScale)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffled) AuraViolet else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
            }

            // Repeat
            val repeatInteraction = remember { MutableInteractionSource() }
            val repeatPressed by repeatInteraction.collectIsPressedAsState()
            val repeatScale by animateFloatAsState(
                targetValue = if (repeatPressed) 0.88f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "repeatScale"
            )
            val repeatIcon = when (repeatMode) {
                PlayerState.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                PlayerState.REPEAT_MODE_ALL -> Icons.Rounded.Repeat
                else -> Icons.Rounded.Repeat
            }
            val repeatTint = when (repeatMode) {
                PlayerState.REPEAT_MODE_OFF -> Color.White.copy(alpha = 0.5f)
                else -> AuraViolet
            }
            IconButton(
                onClick = onCycleRepeat,
                interactionSource = repeatInteraction,
                modifier = Modifier.scale(repeatScale)
            ) {
                Icon(
                    imageVector = repeatIcon,
                    contentDescription = "Repeat mode",
                    tint = repeatTint,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun SpringIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp,
    iconSize: Dp,
    glowColor: Color,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale_$contentDescription"
    )

    val glowModifier = if (glowColor != Color.Transparent) {
        Modifier.auraGlow(glowColor, glowRadius = if (isPrimary) 20.dp else 12.dp)
    } else Modifier

    GlassCard(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .then(glowModifier),
        shape = SquircleShape,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}