// MIT License
// Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.nowplaying.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kibriya.aura.domain.model.RepeatMode
import com.kibriya.aura.ui.theme.GlassCard

@Composable
fun PlayerControlsSection(
    isPlaying: Boolean,
    isShuffled: Boolean,
    repeatMode: RepeatMode,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ControlIconButton(
                icon = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                onClick = onSkipPrevious,
                size = 56.dp,
                iconSize = 28.dp
            )
            ControlIconButton(
                icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                onClick = if (isPlaying) onPause else onPlay,
                size = 72.dp,
                iconSize = 38.dp,
                isPrimary = true
            )
            ControlIconButton(
                icon = Icons.Rounded.SkipNext,
                contentDescription = "Next",
                onClick = onSkipNext,
                size = 56.dp,
                iconSize = 28.dp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val shuffleInteraction = remember { MutableInteractionSource() }
            val shufflePressed by shuffleInteraction.collectIsPressedAsState()
            val shuffleScale by animateFloatAsState(
                targetValue = if (shufflePressed) 0.88f else 1f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
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
                    tint = if (isShuffled) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
            }

            val repeatInteraction = remember { MutableInteractionSource() }
            val repeatPressed by repeatInteraction.collectIsPressedAsState()
            val repeatScale by animateFloatAsState(
                targetValue = if (repeatPressed) 0.88f else 1f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                label = "repeatScale"
            )
            val repeatIcon = when (repeatMode) {
                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                RepeatMode.ALL -> Icons.Rounded.Repeat
                RepeatMode.OFF -> Icons.Rounded.Repeat
            }
            val repeatTint = when (repeatMode) {
                RepeatMode.OFF -> Color.White.copy(alpha = 0.5f)
                else -> Color(0xFF8B5CF6)
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
private fun ControlIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp,
    iconSize: Dp,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "scale_$contentDescription"
    )

    GlassCard(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .then(
                if (isPrimary) Modifier.border(
                    1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), RoundedCornerShape(50)
                ) else Modifier
            )
            .clip(RoundedCornerShape(50)),
        cornerRadius = size / 2
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
        ) {
            IconButton(onClick = onClick, interactionSource = interactionSource) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}