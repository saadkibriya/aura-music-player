// MIT License — Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.preferences.UserPreferences
import com.kibriya.aura.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val songRepository: SongRepository
) : ViewModel() {

    val crossfadeDuration: StateFlow<Int> =
        prefs.crossfadeDuration.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val replayGainEnabled: StateFlow<Boolean> =
        prefs.replayGainEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val gaplessEnabled: StateFlow<Boolean> =
        prefs.gaplessEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val sleepTimerMinutes: StateFlow<Int> =
        prefs.sleepTimerMinutes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val sleepFadeOut: StateFlow<Boolean> =
        prefs.sleepFadeOut.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val accentColor: StateFlow<String> =
        prefs.accentColor.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "violet")

    fun setCrossfadeDuration(v: Int)   { viewModelScope.launch { prefs.setCrossfadeDuration(v) } }
    fun setReplayGainEnabled(v: Boolean) { viewModelScope.launch { prefs.setReplayGainEnabled(v) } }
    fun setGaplessEnabled(v: Boolean)  { viewModelScope.launch { prefs.setGaplessEnabled(v) } }
    fun setSleepTimerMinutes(v: Int)   { viewModelScope.launch { prefs.setSleepTimerMinutes(v) } }
    fun setSleepFadeOut(v: Boolean)    { viewModelScope.launch { prefs.setSleepFadeOut(v) } }
    fun setAccentColor(v: String)      { viewModelScope.launch { prefs.setAccentColor(v) } }

    fun rescanLibrary() {
        viewModelScope.launch { songRepository.triggerRescan() }
    }

    fun clearCache() {
        viewModelScope.launch { songRepository.clearArtworkCache() }
    }
}