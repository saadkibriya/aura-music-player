// MIT License
// Copyright (c) 2025 Md Golam Kibriya
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
    const val LIBRARY    = "library"
    const val NOW_PLAYING = "now_playing"
    const val ALBUM_DETAIL  = "album_detail/{albumId}"
    const val ARTIST_DETAIL = "artist_detail/{artistId}"
    const val SEARCH     = "search"
    const val EQUALISER  = "equaliser"

    fun albumDetail(albumId: Long)   = "album_detail/$albumId"
    fun artistDetail(artistId: Long) = "artist_detail/$artistId"
}

private val enterTransition = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.95f)
private val exitTransition  = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.95f)

@Composable
fun AuraNavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = AuraDestinations.LIBRARY,
        enterTransition    = { enterTransition },
        exitTransition     = { exitTransition },
        popEnterTransition = { enterTransition },
        popExitTransition  = { exitTransition }
    ) {
        composable(AuraDestinations.LIBRARY) {
            LibraryScreen(
                onAlbumClick  = { albumId  -> navController.navigate(AuraDestinations.albumDetail(albumId)) },
                onArtistClick = { artistId -> navController.navigate(AuraDestinations.artistDetail(artistId)) },
                onSongClick   = { navController.navigate(AuraDestinations.NOW_PLAYING) }
            )
        }

        composable(AuraDestinations.NOW_PLAYING) {
            NowPlayingScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route     = AuraDestinations.ALBUM_DETAIL,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) {
            LibraryScreen(
                onAlbumClick  = { id -> navController.navigate(AuraDestinations.albumDetail(id)) },
                onArtistClick = { id -> navController.navigate(AuraDestinations.artistDetail(id)) },
                onSongClick   = { navController.navigate(AuraDestinations.NOW_PLAYING) }
            )
        }

        composable(
            route     = AuraDestinations.ARTIST_DETAIL,
            arguments = listOf(navArgument("artistId") { type = NavType.LongType })
        ) {
            LibraryScreen(
                onAlbumClick  = { id -> navController.navigate(AuraDestinations.albumDetail(id)) },
                onArtistClick = { id -> navController.navigate(AuraDestinations.artistDetail(id)) },
                onSongClick   = { navController.navigate(AuraDestinations.NOW_PLAYING) }
            )
        }

        composable(AuraDestinations.SEARCH) {
            SearchScreen(
                onNavigateToNowPlaying = { navController.navigate(AuraDestinations.NOW_PLAYING) },
                onNavigateToAlbum  = { albumId  -> navController.navigate(AuraDestinations.albumDetail(albumId)) },
                onNavigateToArtist = { artistId -> navController.navigate(AuraDestinations.artistDetail(artistId)) },
                onNavigateBack     = { navController.popBackStack() }
            )
        }

        composable(AuraDestinations.EQUALISER) {
            // placeholder
        }
    }
}