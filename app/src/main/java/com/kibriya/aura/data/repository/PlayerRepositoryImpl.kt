// MIT License
// Copyright (c) 2024 Project Aura

package com.kibriya.aura.data.repository

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.kibriya.aura.domain.model.PlayerState
import com.kibriya.aura.domain.model.RepeatMode
import com.kibriya.aura.domain.model.Song
import com.kibriya.aura.domain.repository.PlayerRepository
import com.kibriya.aura.service.AudioPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PlayerRepository {

    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    init {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, AudioPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
        }, MoreExecutors.directExecutor())
    }

    private fun Song.toMediaItem(): MediaItem =
        MediaItem.fromUri(uri)

    override suspend fun play(song: Song) {
        mediaController?.apply {
            setMediaItem(song.toMediaItem())
            prepare()
            play()
        }
    }

    override suspend fun pause() {
        mediaController?.pause()
    }

    override suspend fun resume() {
        mediaController?.play()
    }

    override suspend fun skipNext() {
        mediaController?.seekToNextMediaItem()
    }

    override suspend fun skipPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    override suspend fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    override suspend fun setQueue(songs: List<Song>, startIndex: Int) {
        mediaController?.apply {
            setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0L)
            prepare()
            play()
        }
    }

    override suspend fun toggleShuffle() {
        mediaController?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }

    override suspend fun cycleRepeatMode() {
        mediaController?.let {
            it.repeatMode = when (it.repeatMode) {
                androidx.media3.common.Player.REPEAT_MODE_OFF -> androidx.media3.common.Player.REPEAT_MODE_ALL
                androidx.media3.common.Player.REPEAT_MODE_ALL -> androidx.media3.common.Player.REPEAT_MODE_ONE
                else -> androidx.media3.common.Player.REPEAT_MODE_OFF
            }
        }
    }

    override suspend fun setSleepTimer(durationMs: Long) {
        _playerState.value = _playerState.value.copy(sleepTimerMs = durationMs)
    }

    override suspend fun cancelSleepTimer() {
        _playerState.value = _playerState.value.copy(sleepTimerMs = 0L)
    }
}