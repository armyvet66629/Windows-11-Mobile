package com.example.windows11mobile.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.windows11mobile.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class NotesViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.notesJson.collect { json ->
                if (json != null) {
                    try {
                        _notes.value = Json.decodeFromString(json)
                    } catch (e: Exception) {}
                }
            }
        }
    }

    private fun saveNotes() {
        viewModelScope.launch {
            settingsRepository.setNotesJson(Json.encodeToString(_notes.value))
        }
    }

    fun addNote(content: String) {
        if (content.isBlank()) return
        val newNote = Note(content = content)
        _notes.value = listOf(newNote) + _notes.value
        saveNotes()
    }

    fun deleteNote(id: String) {
        _notes.value = _notes.value.filter { it.id != id }
        saveNotes()
    }
}
