/*
 * MIT License
 * Copyright (c) 2024 Saad Kibriya
 */

package com.kibriya.aura.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object PreferencesKeys {
        val EQ_ENABLED         = booleanPreferencesKey("eq_enabled")
        val BAND_GAINS         = stringPreferencesKey("band_gains")
        val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
        val REPLAY_GAIN        = booleanPreferencesKey("replay_gain_enabled")
        val LAST_SONG_ID       = longPreferencesKey("last_song_id")
        val LAST_POSITION_MS   = longPreferencesKey("last_position_ms")
        val IS_SHUFFLED        = booleanPreferencesKey("is_shuffled")
        val REPEAT_MODE        = stringPreferencesKey("repeat_mode")
        val GAPLESS_ENABLED    = booleanPreferencesKey("gapless_enabled")
        val ACCENT_COLOR       = stringPreferencesKey("accent_color")
    }

    // ── EQ ───────────────────────────────────────────────────────────────────

    val isEqEnabled: Flow<Boolean> = dataStore.data.map { it[EQ_ENABLED] ?: false }

    suspend fun setEqEnabled(enabled: Boolean) {
        dataStore.edit { it[EQ_ENABLED] = enabled }
    }

    val bandGains: Flow<List<Float>> = dataStore.data.map { prefs ->
        prefs[BAND_GAINS]
            ?.split(",")
            ?.mapNotNull { it.toFloatOrNull() }
            ?.takeIf { it.size == 10 }
            ?: List(10) { 0f }
    }

    suspend fun setBandGains(gains: List<Float>) {
        dataStore.edit { it[BAND_GAINS] = gains.joinToString(",") }
    }

    // ── Crossfade ─────────────────────────────────────────────────────────────

    val crossfadeDuration: Flow<Int> = dataStore.data.map { it[CROSSFADE_DURATION] ?: 0 }

    fun getCrossfadeDuration(): Flow<Int> = crossfadeDuration

    suspend fun setCrossfadeDuration(seconds: Int) {
        dataStore.edit { it[CROSSFADE_DURATION] = seconds }
    }

    // ── Replay Gain ───────────────────────────────────────────────────────────

    val replayGainEnabled: Flow<Boolean> = dataStore.data.map { it[REPLAY_GAIN] ?: false }

    suspend fun setReplayGainEnabled(enabled: Boolean) {
        dataStore.edit { it[REPLAY_GAIN] = enabled }
    }

    // ── Last Played ───────────────────────────────────────────────────────────

    val lastSongId: Flow<Long> = dataStore.data.map { it[LAST_SONG_ID] ?: -1L }

    val lastPositionMs: Flow<Long> = dataStore.data.map { it[LAST_POSITION_MS] ?: 0L }

    fun getLastPlayedSongId(): Flow<Long> = lastSongId

    suspend fun saveLastPlayed(songId: Long, positionMs: Long) {
        dataStore.edit {
            it[LAST_SONG_ID]     = songId
            it[LAST_POSITION_MS] = positionMs
        }
    }

    // ── Playback ──────────────────────────────────────────────────────────────

    val isShuffled: Flow<Boolean> = dataStore.data.map { it[IS_SHUFFLED] ?: false }

    suspend fun setShuffled(shuffled: Boolean) {
        dataStore.edit { it[IS_SHUFFLED] = shuffled }
    }

    val repeatMode: Flow<String> = dataStore.data.map { it[REPEAT_MODE] ?: "NONE" }

    suspend fun setRepeatMode(mode: String) {
        dataStore.edit { it[REPEAT_MODE] = mode }
    }

    val gaplessEnabled: Flow<Boolean> = dataStore.data.map { it[GAPLESS_ENABLED] ?: false }

    suspend fun setGaplessEnabled(enabled: Boolean) {
        dataStore.edit { it[GAPLESS_ENABLED] = enabled }
    }

    // ── Theme ─────────────────────────────────────────────────────────────────

    val accentColor: Flow<String> = dataStore.data.map { it[ACCENT_COLOR] ?: "#6650A4" }

    suspend fun setAccentColor(color: String) {
        dataStore.edit { it[ACCENT_COLOR] = color }
    }
}