// MIT License
// Copyright (c) 2025 Md Golam Kibriya
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
        viewModelScope.launch { userPreferences.crossfadeDuration.collect  { _crossfadeDuration.value = it } }
        viewModelScope.launch { userPreferences.replayGainEnabled.collect  { _replayGainEnabled.value = it } }
        viewModelScope.launch { userPreferences.gaplessEnabled.collect     { _gaplessEnabled.value    = it } }
        viewModelScope.launch { userPreferences.accentColor.collect        { _accentColor.value       = it } }
    }

    fun setCrossfade(s: Int) {
        viewModelScope.launch { _crossfadeDuration.value = s; userPreferences.setCrossfadeDuration(s) }
    }

    fun toggleReplayGain() {
        viewModelScope.launch {
            val v = !_replayGainEnabled.value
            _replayGainEnabled.value = v
            userPreferences.setReplayGainEnabled(v)
        }
    }

    fun toggleGapless() {
        viewModelScope.launch {
            val v = !_gaplessEnabled.value
            _gaplessEnabled.value = v
            userPreferences.setGaplessEnabled(v)
        }
    }

    fun setSleepTimer(ms: Long) { _sleepTimerMs.value = ms }

    fun setAccentColor(hex: String) {
        viewModelScope.launch { _accentColor.value = hex; userPreferences.setAccentColor(hex) }
    }

    fun triggerRescan() {
        viewModelScope.launch { songRepository.triggerScan() }
    }
}