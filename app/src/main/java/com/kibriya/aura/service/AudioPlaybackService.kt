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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.kibriya.aura.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.audio.TeeAudioProcessor
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.kibriya.aura.data.local.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(UnstableApi::class)
class AudioPlaybackService : MediaSessionService() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "aura_playback_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Aura Playback"
        /** Crossfade step interval in ms */
        private const val CROSSFADE_STEP_MS = 50L
        /** Volume fade steps for sleep timer */
        private const val SLEEP_FADE_STEPS = 40
        private const val SLEEP_FADE_STEP_MS = 100L
    }

    @Inject lateinit var userPreferences: UserPreferences

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Sleep timer state
    private var sleepTimerJob: Job? = null
    private var sleepTimerEndMs: Long = 0L

    // Crossfade duration (seconds); 0 = disabled
    private var crossfadeDurationSec: Int = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        serviceScope.launch {
            crossfadeDurationSec = userPreferences.getCrossfadeDuration()
        }
        buildPlayer()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    private fun buildPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
            .also { exo ->
                exo.repeatMode = Player.REPEAT_MODE_OFF
                exo.shuffleModeEnabled = false
                // Gapless: ExoPlayer handles this natively when items are queued
                exo.addListener(object : Player.Listener {
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        if (crossfadeDurationSec > 0 &&
                            reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
                        ) {
                            applyCrossfade(crossfadeDurationSec)
                        }
                    }
                })
            }
    }

    /**
     * Simple crossfade: fades volume to 0 over [durationSec] seconds then back to 1.
     * Real crossfade (two simultaneous streams) requires a custom AudioProcessor or
     * two ExoPlayer instances; this is the single-player volume-ramp approximation.
     */
    private fun applyCrossfade(durationSec: Int) {
        serviceScope.launch {
            val steps = (durationSec * 1000L / CROSSFADE_STEP_MS).toInt().coerceAtLeast(1)
            val stepDown = 1f / steps
            // Fade out
            var vol = player.volume
            repeat(steps) {
                vol = (vol - stepDown).coerceAtLeast(0f)
                player.volume = vol
                delay(CROSSFADE_STEP_MS)
            }
            // Fade in
            val stepUp = 1f / steps
            repeat(steps) {
                vol = (vol + stepUp).coerceAtMost(1f)
                player.volume = vol
                delay(CROSSFADE_STEP_MS)
            }
            player.volume = 1f
        }
    }

    // ── Sleep Timer ──────────────────────────────────────────────────────────

    fun startSleepTimer(durationMs: Long) {
        sleepTimerJob?.cancel()
        sleepTimerEndMs = System.currentTimeMillis() + durationMs
        sleepTimerJob = serviceScope.launch {
            delay(durationMs)
            fadeOutAndStop()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        sleepTimerEndMs = 0L
        // Restore volume in case a fade was in progress
        player.volume = 1f
    }

    fun remainingSleepMs(): Long =
        if (sleepTimerEndMs == 0L) 0L
        else (sleepTimerEndMs - System.currentTimeMillis()).coerceAtLeast(0L)

    private suspend fun fadeOutAndStop() {
        val stepDown = player.volume / SLEEP_FADE_STEPS
        repeat(SLEEP_FADE_STEPS) {
            player.volume = (player.volume - stepDown).coerceAtLeast(0f)
            delay(SLEEP_FADE_STEP_MS)
        }
        player.pause()
        player.volume = 1f
        sleepTimerEndMs = 0L
    }

    // ── Notification Channel ─────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Aura music playback controls"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    // ── MediaSessionService ──────────────────────────────────────────────────

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.launch {
            // Persist last position
            player.currentMediaItem?.mediaId?.toLongOrNull()?.let { id ->
                userPreferences.saveLastPlayed(id, player.currentPosition)
            }
        }
        mediaSession.release()
        player.release()
        serviceScope.cancel()
        super.onDestroy()
    }
}