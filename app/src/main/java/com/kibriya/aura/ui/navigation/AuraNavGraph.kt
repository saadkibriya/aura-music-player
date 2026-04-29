// MIT License
// Copyright (c) 2025 Md Golam Kibriya
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.

package com.kibriya.aura.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kibriya.aura.ui.library.LibraryScreen
import com.kibriya.aura.ui.nowplaying.NowPlayingScreen
import com.kibriya.aura.ui.search.SearchScreen

object AuraDestinations {
    const val LIBRARY = "library"
    const val NOW_PLAYING = "now_playing"
    const val ALBUM_DETAIL = "album_detail/{albumId}"
    const val ARTIST_DETAIL = "artist_detail/{artistId}"
    const val FOLDER_BROWSER = "folder_browser"
    const val SEARCH = "search"
    const val EQUALISER = "equaliser"

    fun albumDetail(albumId: Long) = "album_detail/$albumId"
    fun artistDetail(artistId: Long) = "artist_detail/$artistId"
}

private val enterTransition = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.95f)
private val exitTransition = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.95f)

@Composable
fun AuraNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AuraDestinations.LIBRARY,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { enterTransition },
        popExitTransition = { exitTransition }
    ) {
        composable(AuraDestinations.LIBRARY) {
            LibraryScreen(
                onNavigateToNowPlaying = { navController.navigate(AuraDestinations.NOW_PLAYING) },
                onNavigateToAlbum = { albumId -> navController.navigate(AuraDestinations.albumDetail(albumId)) },
                onNavigateToArtist = { artistId -> navController.navigate(AuraDestinations.artistDetail(artistId)) },
                onNavigateToSearch = { navController.navigate(AuraDestinations.SEARCH) }
            )
        }

        composable(AuraDestinations.NOW_PLAYING) {
            NowPlayingScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = AuraDestinations.ALBUM_DETAIL,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: return@composable
            // AlbumDetailScreen — Module 5 placeholder, implemented in Module 6+
            LibraryScreen(
                onNavigateToNowPlaying = { navController.navigate(AuraDestinations.NOW_PLAYING) },
                onNavigateToAlbum = { id -> navController.navigate(AuraDestinations.albumDetail(id)) },
                onNavigateToArtist = { id -> navController.navigate(AuraDestinations.artistDetail(id)) },
                onNavigateToSearch = { navController.navigate(AuraDestinations.SEARCH) }
            )
        }

        composable(
            route = AuraDestinations.ARTIST_DETAIL,
            arguments = listOf(navArgument("artistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getLong("artistId") ?: return@composable
            LibraryScreen(
                onNavigateToNowPlaying = { navController.navigate(AuraDestinations.NOW_PLAYING) },
                onNavigateToAlbum = { id -> navController.navigate(AuraDestinations.albumDetail(id)) },
                onNavigateToArtist = { id -> navController.navigate(AuraDestinations.artistDetail(id)) },
                onNavigateToSearch = { navController.navigate(AuraDestinations.SEARCH) }
            )
        }

        composable(AuraDestinations.FOLDER_BROWSER) {
            LibraryScreen(
                onNavigateToNowPlaying = { navController.navigate(AuraDestinations.NOW_PLAYING) },
                onNavigateToAlbum = { id -> navController.navigate(AuraDestinations.albumDetail(id)) },
                onNavigateToArtist = { id -> navController.navigate(AuraDestinations.artistDetail(id)) },
                onNavigateToSearch = { navController.navigate(AuraDestinations.SEARCH) }
            )
        }

        composable(AuraDestinations.SEARCH) {
            SearchScreen(
                onNavigateToNowPlaying = { navController.navigate(AuraDestinations.NOW_PLAYING) },
                onNavigateToAlbum = { albumId -> navController.navigate(AuraDestinations.albumDetail(albumId)) },
                onNavigateToArtist = { artistId -> navController.navigate(AuraDestinations.artistDetail(artistId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AuraDestinations.EQUALISER) {
            // EqualiserScreen — Module 6
        }
    }
}