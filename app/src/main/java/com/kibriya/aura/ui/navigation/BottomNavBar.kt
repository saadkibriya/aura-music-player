// MIT License
// Copyright (c) 2025 Md Golam Kibriya

package com.kibriya.aura.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kibriya.aura.ui.theme.AuraTheme
import com.kibriya.aura.ui.theme.glassBackground

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Library", Icons.Rounded.LibraryMusic, AuraDestinations.LIBRARY),
    BottomNavItem("Search", Icons.Rounded.Search, AuraDestinations.SEARCH),
    BottomNavItem("Playlists", Icons.Rounded.QueueMusic, "playlists"),
    BottomNavItem("Settings", Icons.Rounded.Tune, "settings")
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .glassBackground()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "iconTint_${item.label}"
                )

                val pillWidth by animateDpAsState(
                    targetValue = if (isSelected) 56.dp else 48.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "pillWidth_${item.label}"
                )

                Box(
                    modifier = Modifier
                        .size(width = pillWidth, height = 44.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            color = if (isSelected) AuraTheme.colors.violet.copy(alpha = 0.35f)
                            else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(item.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}