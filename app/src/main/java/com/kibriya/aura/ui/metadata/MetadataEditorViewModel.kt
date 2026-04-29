// MIT License — Copyright (c) 2025 Md Golam Kibriya
package com.kibriya.aura.ui.metadata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kibriya.aura.data.db.dao.SongDao
import com.kibriya.aura.data.db.entity.SongEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import javax.inject.Inject

sealed class SaveState {
    object Idle    : SaveState()
    object Saving  : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

@HiltViewModel
class MetadataEditorViewModel @Inject constructor(
    private val songDao: SongDao
) : ViewModel() {

    private var currentSong: SongEntity? = null

    val title  = MutableStateFlow("")
    val artist = MutableStateFlow("")
    val album  = MutableStateFlow("")
    val year   = MutableStateFlow("")
    val genre  = MutableStateFlow("")
    val artUri = MutableStateFlow<String?>(null)

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun loadSong(id: Long) {
        viewModelScope.launch {
            val song = songDao.getSongById(id) ?: return@launch
            currentSong = song
            title.value  = song.title
            artist.value = song.artist ?: ""
            album.value  = song.album ?: ""
            year.value   = song.year?.toString() ?: ""
            genre.value  = song.genre ?: ""
            artUri.value = song.artUri
        }
    }

    fun updateTitle(v: String)  { title.value  = v }
    fun updateArtist(v: String) { artist.value = v }
    fun updateAlbum(v: String)  { album.value  = v }
    fun updateYear(v: String)   { year.value   = v }
    fun updateGenre(v: String)  { genre.value  = v }
    fun updateArtUri(v: String) { artUri.value = v }

    fun saveMetadata() {
        val song = currentSong ?: return
        _saveState.value = SaveState.Saving
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Write ID3 tags via JAudioTagger
                val file = File(song.path)
                val audioFile = AudioFileIO.read(file)
                val tag = audioFile.tagOrCreateAndSetDefault
                tag.setField(FieldKey.TITLE,  title.value)
                tag.setField(FieldKey.ARTIST, artist.value)
                tag.setField(FieldKey.ALBUM,  album.value)
                tag.setField(FieldKey.YEAR,   year.value)
                tag.setField(FieldKey.GENRE,  genre.value)
                AudioFileIO.write(audioFile)

                // Sync Room DB
                songDao.updateSong(
                    song.copy(
                        title  = title.value,
                        artist = artist.value.ifBlank { null },
                        album  = album.value.ifBlank { null },
                        year   = year.value.toIntOrNull(),
                        genre  = genre.value.ifBlank { null },
                        artUri = artUri.value
                    )
                )
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}