/*
 * MIT License
 * Copyright (c) 2024 Md Golam Kibriya
 */

package com.kibriya.aura.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kibriya.aura.data.local.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val lastPlayedSongId = runBlocking { userPreferences.getLastPlayedSongId().first() }

        if (lastPlayedSongId == -1L) return

        val serviceIntent = Intent(context, AudioPlaybackService::class.java).apply {
            putExtra("RESUME_SONG_ID", lastPlayedSongId)
        }
        context.startForegroundService(serviceIntent)
    }
}