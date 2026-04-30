/*
 * MIT License
 * Copyright (c) 2024 Saad Kibriya
 */

package com.kibriya.aura.ui.equaliser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.audio.AuraAudioProcessor
import com.kibriya.aura.data.preferences.UserPreferences
import com.kibriya.aura.domain.model.EqPreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EqualiserViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val audioProcessor: AuraAudioProcessor
) : ViewModel() {

    val bandGains: StateFlow<List<Float>> = userPreferences.bandGains
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = List(10) { 0f }
        )

    val isEqEnabled: StateFlow<Boolean> = userPreferences.isEqEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val crossfadeDuration: StateFlow<Int> = userPreferences.crossfadeDuration
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val replayGainEnabled: StateFlow<Boolean> = userPreferences.replayGainEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun setBandGain(band: Int, gain: Float) {
        viewModelScope.launch {
            val current = bandGains.value.toMutableList()
            if (band in current.indices) {
                current[band] = gain
            }
            val newList = current.toList()
            userPreferences.setBandGains(newList)
            audioProcessor.setBandGain(band, gain)
        }
    }

    fun toggleEq() {
        viewModelScope.launch {
            val current = isEqEnabled.value
            userPreferences.setEqEnabled(!current)
            audioProcessor.setEqEnabled(!current)
        }
    }

    fun setCrossfade(s: Int) {
        viewModelScope.launch {
            userPreferences.setCrossfadeDuration(s)
        }
    }

    fun toggleReplayGain() {
        viewModelScope.launch {
            val current = replayGainEnabled.value
            userPreferences.setReplayGainEnabled(!current)
            audioProcessor.setReplayGainEnabled(!current)
        }
    }

    fun applyPreset(preset: EqPreset) {
        viewModelScope.launch {
            userPreferences.setBandGains(preset.gains)
            preset.gains.forEachIndexed { band, gain ->
                audioProcessor.setBandGain(band, gain)
            }
        }
    }
}