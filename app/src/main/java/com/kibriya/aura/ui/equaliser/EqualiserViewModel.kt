// MIT License
//
// Copyright (c) 2024 Saad Kibriya
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package com.kibriya.aura.ui.equaliser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.audio.AuraAudioProcessor
import com.kibriya.aura.data.local.preferences.UserPreferences
import com.kibriya.aura.domain.model.EqPreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EqualiserViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val audioProcessor: AuraAudioProcessor
) : ViewModel() {

    private val _bandGains = MutableStateFlow(List(10) { 0f })
    val bandGains: StateFlow<List<Float>> = _bandGains.asStateFlow()

    private val _isEqEnabled = MutableStateFlow(false)
    val isEqEnabled: StateFlow<Boolean> = _isEqEnabled.asStateFlow()

    private val _selectedPreset = MutableStateFlow<EqPreset?>(null)
    val selectedPreset: StateFlow<EqPreset?> = _selectedPreset.asStateFlow()

    private val _crossfadeDuration = MutableStateFlow(0)
    val crossfadeDuration: StateFlow<Int> = _crossfadeDuration.asStateFlow()

    private val _replayGainEnabled = MutableStateFlow(false)
    val replayGainEnabled: StateFlow<Boolean> = _replayGainEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.isEqEnabled.collect { _isEqEnabled.value = it }
        }
        viewModelScope.launch {
            userPreferences.bandGains.collect { _bandGains.value = it }
        }
        viewModelScope.launch {
            userPreferences.crossfadeDuration.collect { _crossfadeDuration.value = it }
        }
        viewModelScope.launch {
            userPreferences.replayGainEnabled.collect { _replayGainEnabled.value = it }
        }
    }

    fun setBandGain(band: Int, gain: Float) {
        viewModelScope.launch {
            val updated = _bandGains.value.toMutableList().also { it[band] = gain }
            _bandGains.value = updated
            userPreferences.setBandGains(updated)
            audioProcessor.setBandGain(band, gain)
        }
    }

    fun applyPreset(preset: EqPreset) {
        viewModelScope.launch {
            _selectedPreset.value = preset
            _bandGains.value = preset.gains
            userPreferences.setBandGains(preset.gains)
            preset.gains.forEachIndexed { index, gain ->
                audioProcessor.setBandGain(index, gain)
            }
        }
    }

    fun toggleEq() {
        viewModelScope.launch {
            val newValue = !_isEqEnabled.value
            _isEqEnabled.value = newValue
            userPreferences.setEqEnabled(newValue)
            audioProcessor.setEqEnabled(newValue)
        }
    }

    fun setCrossfade(seconds: Int) {
        viewModelScope.launch {
            _crossfadeDuration.value = seconds
            userPreferences.setCrossfadeDuration(seconds)
        }
    }

    fun toggleReplayGain() {
        viewModelScope.launch {
            val newValue = !_replayGainEnabled.value
            _replayGainEnabled.value = newValue
            userPreferences.setReplayGainEnabled(newValue)
            audioProcessor.setReplayGainEnabled(newValue)
        }
    }
}