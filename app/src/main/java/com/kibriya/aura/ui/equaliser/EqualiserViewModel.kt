/*
 * MIT License
 * Copyright (c) 2024 Md Golam Kibriya
 */
package com.kibriya.aura.ui.equaliser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.audio.AuraAudioProcessor
import com.kibriya.aura.data.local.preferences.UserPreferences
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val isEqEnabled: StateFlow<Boolean> = userPreferences.isEqEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val crossfadeDuration: StateFlow<Int> = userPreferences.crossfadeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val replayGainEnabled: StateFlow<Boolean> = userPreferences.replayGainEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setBandGain(band: Int, gain: Float) {
        viewModelScope.launch {
            val current = bandGains.value.toMutableList()
            if (band < current.size) {
                current[band] = gain
            } else {
                while (current.size <= band) current.add(0f)
                current[band] = gain
            }
            userPreferences.setBandGains(current)
            audioProcessor.setBandGain(band, gain)
        }
    }

    fun toggleEq() {
        viewModelScope.launch {
            userPreferences.setEqEnabled(!isEqEnabled.value)
        }
    }

    fun setCrossfade(seconds: Int) {
        viewModelScope.launch {
            userPreferences.setCrossfadeDuration(seconds)
        }
    }

    fun toggleReplayGain() {
        viewModelScope.launch {
            userPreferences.setReplayGainEnabled(!replayGainEnabled.value)
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