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

package com.kibriya.aura.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.local.preferences.UserPreferences
import com.kibriya.aura.domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _crossfadeDuration = MutableStateFlow(0)
    val crossfadeDuration: StateFlow<Int> = _crossfadeDuration.asStateFlow()

    private val _replayGainEnabled = MutableStateFlow(false)
    val replayGainEnabled: StateFlow<Boolean> = _replayGainEnabled.asStateFlow()

    private val _gaplessEnabled = MutableStateFlow(false)
    val gaplessEnabled: StateFlow<Boolean> = _gaplessEnabled.asStateFlow()

    private val _sleepTimerMs = MutableStateFlow(0L)
    val sleepTimerMs: StateFlow<Long> = _sleepTimerMs.asStateFlow()

    private val _accentColor = MutableStateFlow("#8B5CF6")
    val accentColor: StateFlow<String> = _accentColor.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.crossfadeDuration.collect { _crossfadeDuration.value = it }
        }
        viewModelScope.launch {
            userPreferences.replayGainEnabled.collect { _replayGainEnabled.value = it }
        }
        viewModelScope.launch {
            userPreferences.gaplessEnabled.collect { _gaplessEnabled.value = it }
        }
        viewModelScope.launch {
            userPreferences.sleepTimerMs.collect { _sleepTimerMs.value = it }
        }
        viewModelScope.launch {
            userPreferences.accentColor.collect { _accentColor.value = it }
        }
    }

    fun setCrossfade(s: Int) {
        viewModelScope.launch {
            _crossfadeDuration.value = s
            userPreferences.setCrossfadeDuration(s)
        }
    }

    fun toggleReplayGain() {
        viewModelScope.launch {
            val newValue = !_replayGainEnabled.value
            _replayGainEnabled.value = newValue
            userPreferences.setReplayGainEnabled(newValue)
        }
    }

    fun toggleGapless() {
        viewModelScope.launch {
            val newValue = !_gaplessEnabled.value
            _gaplessEnabled.value = newValue
            userPreferences.setGaplessEnabled(newValue)
        }
    }

    fun setSleepTimer(ms: Long) {
        viewModelScope.launch {
            _sleepTimerMs.value = ms
            userPreferences.setSleepTimerMs(ms)
        }
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch {
            _accentColor.value = hex
            userPreferences.setAccentColor(hex)
        }
    }

    fun triggerRescan() {
        viewModelScope.launch {
            songRepository.triggerScan()
        }
    }
}