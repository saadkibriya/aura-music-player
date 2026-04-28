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

package com.kibriya.aura.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        const val NO_SONG = -1L

        // Keys
        private val KEY_LAST_SONG_ID        = longPreferencesKey("last_song_id")
        private val KEY_LAST_POSITION_MS    = longPreferencesKey("last_position_ms")
        private val KEY_REPEAT_MODE         = stringPreferencesKey("repeat_mode")
        private val KEY_IS_SHUFFLED         = booleanPreferencesKey("is_shuffled")
        private val KEY_SLEEP_TIMER_DURATION= longPreferencesKey("sleep_timer_duration_ms")
        private val KEY_EQ_ENABLED          = booleanPreferencesKey("eq_enabled")
        private val KEY_CROSSFADE_DURATION  = intPreferencesKey("crossfade_duration_sec")
        private val KEY_REPLAY_GAIN_ENABLED = booleanPreferencesKey("replay_gain_enabled")
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    suspend fun getLastPlayedSongId(): Long =
        dataStore.data.first()[KEY_LAST_SONG_ID] ?: NO_SONG

    suspend fun getLastPositionMs(): Long =
        dataStore.data.first()[KEY_LAST_POSITION_MS] ?: 0L

    suspend fun getRepeatMode(): String =
        dataStore.data.first()[KEY_REPEAT_MODE] ?: "OFF"

    suspend fun getIsShuffled(): Boolean =
        dataStore.data.first()[KEY_IS_SHUFFLED] ?: false

    suspend fun getSleepTimerDuration(): Long =
        dataStore.data.first()[KEY_SLEEP_TIMER_DURATION] ?: 0L

    suspend fun isEqEnabled(): Boolean =
        dataStore.data.first()[KEY_EQ_ENABLED] ?: false

    suspend fun getCrossfadeDuration(): Int =
        dataStore.data.first()[KEY_CROSSFADE_DURATION] ?: 0

    suspend fun isReplayGainEnabled(): Boolean =
        dataStore.data.first()[KEY_REPLAY_GAIN_ENABLED] ?: false

    // ── Flows (for observing) ─────────────────────────────────────────────────

    val repeatModeFlow: Flow<String> =
        dataStore.data.map { it[KEY_REPEAT_MODE] ?: "OFF" }

    val isShuffledFlow: Flow<Boolean> =
        dataStore.data.map { it[KEY_IS_SHUFFLED] ?: false }

    val eqEnabledFlow: Flow<Boolean> =
        dataStore.data.map { it[KEY_EQ_ENABLED] ?: false }

    val crossfadeDurationFlow: Flow<Int> =
        dataStore.data.map { it[KEY_CROSSFADE_DURATION] ?: 0 }

    val replayGainEnabledFlow: Flow<Boolean> =
        dataStore.data.map { it[KEY_REPLAY_GAIN_ENABLED] ?: false }

    // ── Writes ────────────────────────────────────────────────────────────────

    suspend fun saveLastPlayed(songId: Long, positionMs: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_SONG_ID]     = songId
            prefs[KEY_LAST_POSITION_MS] = positionMs
        }
    }

    suspend fun saveRepeatMode(mode: String) {
        dataStore.edit { it[KEY_REPEAT_MODE] = mode }
    }

    suspend fun saveIsShuffled(shuffled: Boolean) {
        dataStore.edit { it[KEY_IS_SHUFFLED] = shuffled }
    }

    suspend fun saveSleepTimerDuration(durationMs: Long) {
        dataStore.edit { it[KEY_SLEEP_TIMER_DURATION] = durationMs }
    }

    suspend fun saveEqEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_EQ_ENABLED] = enabled }
    }

    suspend fun saveCrossfadeDuration(seconds: Int) {
        dataStore.edit { it[KEY_CROSSFADE_DURATION] = seconds }
    }

    suspend fun saveReplayGainEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_REPLAY_GAIN_ENABLED] = enabled }
    }
}