// MIT License
// Copyright (c) 2024 Saad Kibriya
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
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.kibriya.aura.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    // ── Keys ────────────────────────────────────────────────────────────────

    private object Keys {
        val lastSongId          = longPreferencesKey("lastSongId")
        val lastPositionMs      = longPreferencesKey("lastPositionMs")
        val repeatMode          = stringPreferencesKey("repeatMode")
        val isShuffled          = booleanPreferencesKey("isShuffled")
        val eqEnabled           = booleanPreferencesKey("eqEnabled")
        val crossfadeDuration   = intPreferencesKey("crossfadeDuration")
        val replayGainEnabled   = booleanPreferencesKey("replayGainEnabled")
    }

    // ── Flows ────────────────────────────────────────────────────────────────

    val lastSongId: Flow<Long> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.lastSongId] ?: -1L }

    val lastPositionMs: Flow<Long> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.lastPositionMs] ?: 0L }

    val repeatMode: Flow<String> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.repeatMode] ?: "OFF" }

    val isShuffled: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.isShuffled] ?: false }

    val eqEnabled: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.eqEnabled] ?: false }

    val crossfadeDuration: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.crossfadeDuration] ?: 0 }

    val replayGainEnabled: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[Keys.replayGainEnabled] ?: false }

    // ── Updaters ─────────────────────────────────────────────────────────────

    suspend fun updateLastSongId(value: Long) {
        dataStore.edit { prefs -> prefs[Keys.lastSongId] = value }
    }

    suspend fun updateLastPositionMs(value: Long) {
        dataStore.edit { prefs -> prefs[Keys.lastPositionMs] = value }
    }

    suspend fun updateRepeatMode(value: String) {
        dataStore.edit { prefs -> prefs[Keys.repeatMode] = value }
    }

    suspend fun updateIsShuffled(value: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.isShuffled] = value }
    }

    suspend fun updateEqEnabled(value: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.eqEnabled] = value }
    }

    suspend fun updateCrossfadeDuration(value: Int) {
        dataStore.edit { prefs -> prefs[Keys.crossfadeDuration] = value }
    }

    suspend fun updateReplayGainEnabled(value: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.replayGainEnabled] = value }
    }
}