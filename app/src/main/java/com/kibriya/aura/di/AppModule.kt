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

package com.kibriya.aura.di

import android.content.ComponentName
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.kibriya.aura.data.repository.PlayerRepositoryImpl
import com.kibriya.aura.data.repository.SongRepositoryImpl
import com.kibriya.aura.domain.repository.PlayerRepository
import com.kibriya.aura.domain.repository.SongRepository
import com.kibriya.aura.service.AudioPlaybackService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "aura_user_preferences")

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    // ── Interface bindings ────────────────────────────────────────────────────

    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: SongRepositoryImpl): SongRepository

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    companion object {

        // ── DataStore ─────────────────────────────────────────────────────────

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.dataStore

        // ── MediaController Future ────────────────────────────────────────────

        /**
         * Provides a [ListenableFuture<MediaController>] that connects to
         * [AudioPlaybackService]. Used by [PlayerRepositoryImpl] to send
         * player commands without binding a Service manually.
         */
        @Provides
        @Singleton
        fun provideMediaControllerFuture(
            @ApplicationContext context: Context
        ): ListenableFuture<MediaController> {
            val sessionToken = SessionToken(
                context,
                ComponentName(context, AudioPlaybackService::class.java)
            )
            return MediaController.Builder(context, sessionToken).buildAsync()
        }
    }
}