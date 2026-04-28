/*
 * MIT License
 *
 * Copyright (c) 2025 Md Golam Kibriya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.kibriya.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kibriya.aura.ui.theme.AuraBlack
import com.kibriya.aura.ui.theme.AuraTheme
import com.kibriya.aura.ui.theme.AuraViolet
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host.
 *
 * Edge-to-edge is enabled here; the Compose tree owns all insets.
 * [WindowCompat.setDecorFitsSystemWindows] is set to false so that
 * content draws behind status and navigation bars.
 *
 * [AuraNavGraph] owns all screen navigation; new destinations are
 * registered there as individual modules are added.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge (transparent bars, content draws behind them)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AuraTheme {
                AuraNavGraph()
            }
        }
    }
}

// ── Navigation ─────────────────────────────────────────────────────────────

/** Top-level navigation destinations. */
internal sealed class Screen(val route: String) {
    data object Library : Screen("library")
    data object NowPlaying : Screen("now_playing")
    data object Search : Screen("search")
    data object Settings : Screen("settings")
}

/**
 * Root NavHost scaffold.
 *
 * All composable destinations use cross-fade transitions so the 120 fps
 * fluid animation budget is not wasted on heavy slide choreography at the
 * navigation level (individual screens own their own entrance animations).
 */
@Composable
private fun AuraNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = Screen.Library.route,
        enterTransition  = { fadeIn(animationSpec  = tween(300)) },
        exitTransition   = { fadeOut(animationSpec = tween(300)) },
    ) {
        composable(Screen.Library.route) {
            LibraryPlaceholderScreen()
        }

        composable(Screen.NowPlaying.route) {
            // Module 2 – NowPlayingScreen goes here
            PlaceholderScreen(label = "Now Playing")
        }

        composable(Screen.Search.route) {
            // Module 3 – SearchScreen goes here
            PlaceholderScreen(label = "Search")
        }

        composable(Screen.Settings.route) {
            // Module 4 – SettingsScreen goes here
            PlaceholderScreen(label = "Settings")
        }
    }
}

// ── Placeholder screens (replaced per module) ──────────────────────────────

@Composable
private fun LibraryPlaceholderScreen() {
    Box(
        modifier           = Modifier
            .fillMaxSize()
            .background(AuraBlack),
        contentAlignment   = Alignment.Center
    ) {
        Text(
            text       = "AURA",
            style      = MaterialTheme.typography.displayLarge.copy(
                fontWeight   = FontWeight.Bold,
                color        = AuraViolet,
                letterSpacing = androidx.compose.ui.unit.TextUnit(
                    value = 12f,
                    type  = androidx.compose.ui.unit.TextUnitType.Sp
                )
            )
        )
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(AuraBlack),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}