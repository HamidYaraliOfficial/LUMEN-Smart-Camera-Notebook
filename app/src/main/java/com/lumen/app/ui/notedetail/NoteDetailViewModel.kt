package com.lumen.app.ui.notedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.app.domain.model.Note
import com.lumen.app.domain.repository.DocumentRepository
import com.lumen.app.domain.repository.NoteRepository
import com.lumen.app.domain.repository.OcrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val documentRepository: DocumentRepository,
    private val ocrRepository: OcrRepository,
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    val note: StateFlow<Note?> = noteRepository.observeById(noteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateBody(newBody: String) {
        val current = note.value ?: return
        viewModelScope.launch {
            noteRepository.upsert(
                current.copy(bodyMarkdown = newBody, version = current.version + 1, updatedAtEpochMs = System.currentTimeMillis())
            )
        }
    }

    fun toggleFavorite() {
        val current = note.value ?: return
        viewModelScope.launch { noteRepository.toggleFavorite(current.id, !current.isFavorite) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            noteRepository.delete(noteId)
            onDeleted()
        }
    }
}
