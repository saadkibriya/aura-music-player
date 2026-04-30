/*
 * MIT License
 * Copyright (c) 2024 Saad Kibriya
 */

package com.kibriya.aura.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.kibriya.aura.R
import com.kibriya.aura.data.preferences.UserPreferences
import com.kibriya.aura.domain.model.Song
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class AudioPlaybackService : MediaSessionService() {

    @Inject
    lateinit var userPreferences: UserPreferences

    private val binder = LocalBinder()

    private lateinit var player: ExoPlayer

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition

    private var songQueue: List<Song> = emptyList()
    private var currentIndex: Int = 0

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "aura_playback_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY = "com.kibriya.aura.ACTION_PLAY"
        const val ACTION_PAUSE = "com.kibriya.aura.ACTION_PAUSE"
        const val ACTION_NEXT = "com.kibriya.aura.ACTION_NEXT"
        const val ACTION_PREV = "com.kibriya.aura.ACTION_PREV"
        const val ACTION_STOP = "com.kibriya.aura.ACTION_STOP"
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Fix line 82: getCrossfadeDuration() returns Flow<Int>, collect it properly
        val crossfadeDuration = runBlocking { userPreferences.getCrossfadeDuration().first() }

        player = ExoPlayer.Builder(this).build()
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                updateNotification()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    skipToNext()
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _playbackPosition.value = newPosition.positionMs
            }
        })

        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_PLAY -> resumePlayback()
            ACTION_PAUSE -> pausePlayback()
            ACTION_NEXT -> skipToNext()
            ACTION_PREV -> skipToPrevious()
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        _currentSong.value = song
        songQueue = queue.ifEmpty { listOf(song) }
        currentIndex = songQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        val mediaItem = MediaItem.fromUri(song.filePath)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        updateNotification()
    }

    fun resumePlayback() {
        player.play()
    }

    fun pausePlayback() {
        player.pause()
        saveCurrentPosition()
    }

    fun skipToNext() {
        if (currentIndex < songQueue.size - 1) {
            currentIndex++
            playSong(songQueue[currentIndex], songQueue)
        }
    }

    fun skipToPrevious() {
        if (player.currentPosition > 3000) {
            player.seekTo(0)
        } else if (currentIndex > 0) {
            currentIndex--
            playSong(songQueue[currentIndex], songQueue)
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _playbackPosition.value = positionMs
    }

    fun getCurrentPosition(): Long = player.currentPosition

    fun getDuration(): Long = player.duration.coerceAtLeast(0L)

    // Fix line 208: replace saveLastPlayed call using coroutine scope launch
    private fun saveCurrentPosition() {
        val song = _currentSong.value ?: return
        val positionMs = player.currentPosition
        lifecycleScope.launch {
            userPreferences.saveLastPlayed(song.id, positionMs)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Aura Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Music playback controls"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val song = _currentSong.value

        val playPauseAction = if (_isPlaying.value) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "Pause",
                buildActionIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "Play",
                buildActionIntent(ACTION_PLAY)
            )
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song?.title ?: "Aura")
            .setContentText(song?.artist ?: "")
            .addAction(R.drawable.ic_skip_previous, "Previous", buildActionIntent(ACTION_PREV))
            .addAction(playPauseAction)
            .addAction(R.drawable.ic_skip_next, "Next", buildActionIntent(ACTION_NEXT))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setOngoing(_isPlaying.value)
            .build()
    }

    private fun buildActionIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        saveCurrentPosition()
        player.release()
        super.onDestroy()
    }
}