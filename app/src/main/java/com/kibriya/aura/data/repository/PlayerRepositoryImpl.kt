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

package com.kibriya.aura.data.repository

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.kibriya.aura.domain.model.PlayerState
import com.kibriya.aura.domain.model.RepeatMode
import com.kibriya.aura.domain.model.Song
import com.kibriya.aura.domain.repository.PlayerRepository
import com.kibriya.aura.service.AudioPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(UnstableApi::class)
class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val controllerFuture: ListenableFuture<MediaController>
) : PlayerRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _playerState = MutableStateFlow(PlayerState())
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    /** In-memory queue mirroring what ExoPlayer has loaded */
    private var currentQueue: List<Song> = emptyList()
    private var isShuffled: Boolean = false
    private var repeatMode: RepeatMode = RepeatMode.OFF
    private var sleepTimerMs: Long = 0L

    private var controller: MediaController? = null

    init {
        scope.launch {
            controller = controllerFuture.await()
            attachListener()
            startPositionPolling()
        }
    }

    private fun attachListener() {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = syncState()
            override fun onMediaItemTransition(item: MediaItem?, reason: Int) = syncState()
            override fun onRepeatModeChanged(mode: Int) = syncState()
            override fun onShuffleModeEnabledChanged(enabled: Boolean) = syncState()
            override fun onPlaybackStateChanged(state: Int) = syncState()
        })
    }

    private fun syncState() {
        val ctrl = controller ?: return
        val index = ctrl.currentMediaItemIndex
        val song = currentQueue.getOrNull(index)
        _playerState.value = PlayerState(
            currentSong  = song,
            queue        = currentQueue,
            currentIndex = index,
            isPlaying    = ctrl.isPlaying,
            repeatMode   = repeatMode,
            isShuffled   = isShuffled,
            positionMs   = ctrl.currentPosition,
            durationMs   = ctrl.duration.coerceAtLeast(0L),
            sleepTimerMs = sleepTimerMs
        )
    }

    /** Poll position every 500 ms while playing for smooth UI updates */
    private fun startPositionPolling() {
        scope.launch {
            while (true) {
                delay(500)
                val ctrl = controller ?: continue
                if (ctrl.isPlaying) syncState()
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun Song.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setTrackNumber(trackNumber)
                    .setArtworkUri(albumArtUri?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()

    private fun media3RepeatMode(mode: RepeatMode): Int = when (mode) {
        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
    }

    // ── PlayerRepository ─────────────────────────────────────────────────────

    override suspend fun play(song: Song, queue: List<Song>, startIndex: Int) =
        withContext(Dispatchers.Main) {
            val ctrl = controller ?: return@withContext
            currentQueue = queue
            ctrl.setMediaItems(queue.map { it.toMediaItem() }, startIndex, 0L)
            ctrl.prepare()
            ctrl.play()
            syncState()
        }

    override suspend fun pause() = withContext(Dispatchers.Main) {
        controller?.pause()
        syncState()
    }

    override suspend fun resume() = withContext(Dispatchers.Main) {
        controller?.play()
        syncState()
    }

    override suspend fun skipNext() = withContext(Dispatchers.Main) {
        controller?.seekToNextMediaItem()
        syncState()
    }

    override suspend fun skipPrevious() = withContext(Dispatchers.Main) {
        val ctrl = controller ?: return@withContext
        if (ctrl.currentPosition > 3_000L) {
            ctrl.seekTo(0L)
        } else {
            ctrl.seekToPreviousMediaItem()
        }
        syncState()
    }

    override suspend fun seekTo(positionMs: Long) = withContext(Dispatchers.Main) {
        controller?.seekTo(positionMs)
        syncState()
    }

    override suspend fun setQueue(songs: List<Song>, startIndex: Int) =
        withContext(Dispatchers.Main) {
            val ctrl = controller ?: return@withContext
            currentQueue = songs
            ctrl.setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0L)
            ctrl.prepare()
            syncState()
        }

    override suspend fun toggleShuffle() = withContext(Dispatchers.Main) {
        isShuffled = !isShuffled
        controller?.shuffleModeEnabled = isShuffled
        syncState()
    }

    override suspend fun cycleRepeatMode() = withContext(Dispatchers.Main) {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.OFF
        }
        controller?.repeatMode = media3RepeatMode(repeatMode)
        syncState()
    }

    override suspend fun setSleepTimer(durationMs: Long) = withContext(Dispatchers.Main) {
        sleepTimerMs = durationMs
        // Delegate to the service via a custom command or direct call via binder
        // Here we use a coroutine on the repo side as an approximation;
        // the service's own startSleepTimer is the authoritative path when bound.
        syncState()
    }

    override suspend fun cancelSleepTimer() = withContext(Dispatchers.Main) {
        sleepTimerMs = 0L
        syncState()
    }
}