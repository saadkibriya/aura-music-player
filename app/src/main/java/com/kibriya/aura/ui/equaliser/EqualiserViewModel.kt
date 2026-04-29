/*
 * MIT License
 * Copyright (c) 2025 Md Golam Kibriya
 */

package com.kibriya.aura.ui.equaliser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.repository.PlayerRepository
import com.kibriya.aura.data.preferences.UserPreferences
import com.kibriya.aura.domain.model.EqPreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EqualiserViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    // ── Exposed state ─────────────────────────────────────────────────────────

    private val _bandGains = MutableStateFlow(List(10) { 0f })
    val bandGains: StateFlow<List<Float>> = _bandGains.asStateFlow()

    private val _isEqEnabled = MutableStateFlow(false)
    val isEqEnabled: StateFlow<Boolean> = _isEqEnabled.asStateFlow()

    private val _selectedPreset = MutableStateFlow<EqPreset?>(EqPreset.FLAT)
    val selectedPreset: StateFlow<EqPreset?> = _selectedPreset.asStateFlow()

    private val _crossfadeDuration = MutableStateFlow(0)
    val crossfadeDuration: StateFlow<Int> = _crossfadeDuration.asStateFlow()

    private val _replayGainEnabled = MutableStateFlow(false)
    val replayGainEnabled: StateFlow<Boolean> = _replayGainEnabled.asStateFlow()

    // ── Init — load persisted prefs ───────────────────────────────────────────

    init {
        viewModelScope.launch {
            // Load EQ enabled state
            userPreferences.eqEnabled.collect { enabled ->
                _isEqEnabled.value = enabled
            }
        }
        viewModelScope.launch {
            userPreferences.eqBandGains.collect { gains ->
                _bandGains.value = gains
                playerRepository.audioProcessor.setAllBandGains(gains)
            }
        }
        viewModelScope.launch {
            userPreferences.crossfadeDuration.collect { secs ->
                _crossfadeDuration.value = secs
            }
        }
        viewModelScope.launch {
            userPreferences.replayGainEnabled.collect { enabled ->
                _replayGainEnabled.value = enabled
            }
        }
    }

    // ── Public methods ────────────────────────────────────────────────────────

    /** Update a single band gain and persist it. */
    fun setBandGain(band: Int, gainDb: Float) {
        val updated = _bandGains.value.toMutableList().also { it[band] = gainDb }
        _bandGains.value = updated
        _selectedPreset.value = null   // custom — no matching preset
        viewModelScope.launch {
            userPreferences.setEqBandGains(updated)
            playerRepository.audioProcessor.setBandGain(band, gainDb)
        }
    }

    /** Apply a named preset, animating all bands to new values. */
    fun applyPreset(preset: EqPreset) {
        _bandGains.value = preset.gains
        _selectedPreset.value = preset
        viewModelScope.launch {
            userPreferences.setEqBandGains(preset.gains)
            playerRepository.audioProcessor.setAllBandGains(preset.gains)
        }
    }

    /** Toggle EQ on/off. */
    fun toggleEq() {
        val next = !_isEqEnabled.value
        _isEqEnabled.value = next
        viewModelScope.launch {
            userPreferences.setEqEnabled(next)
            // When disabled, flatten all bands to 0
            if (!next) {
                playerRepository.audioProcessor.setAllBandGains(List(10) { 0f })
            } else {
                playerRepository.audioProcessor.setAllBandGains(_bandGains.value)
            }
        }
    }

    /** Set crossfade duration (0–10 seconds). */
    fun setCrossfade(seconds: Int) {
        val clamped = seconds.coerceIn(0, 10)
        _crossfadeDuration.value = clamped
        viewModelScope.launch {
            userPreferences.setCrossfadeDuration(clamped)
            playerRepository.setCrossfadeDuration(clamped)
        }
    }

    /** Toggle ReplayGain normalisation. */
    fun toggleReplayGain() {
        val next = !_replayGainEnabled.value
        _replayGainEnabled.value = next
        viewModelScope.launch {
            userPreferences.setReplayGainEnabled(next)
            // Pass 0f (bypass) when disabled, 1f (unity gain) as base when enabled
            playerRepository.audioProcessor.setReplayGain(if (next) 1f else 0f)
        }
    }
}