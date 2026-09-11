package com.lumen.app.ui.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.app.domain.model.DefaultNotebooks
import com.lumen.app.domain.model.Notebook
import com.lumen.app.domain.repository.NotebookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotebookViewModel @Inject constructor(
    private val repository: NotebookRepository
) : ViewModel() {

    val notebooks: StateFlow<List<Notebook>> = repository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Seeds the built-in starter notebooks the first time the list is empty. Safe to call
     * repeatedly — it only inserts when there are currently zero notebooks. */
    fun seedDefaultsIfEmpty() {
        viewModelScope.launch {
            val existing = repository.observeActive()
            // one-shot check against the current DB state rather than the (possibly stale) StateFlow
            val isEmpty = notebooks.value.isEmpty()
            if (isEmpty) {
                DefaultNotebooks.PRESETS.forEach { (name, color) ->
                    repository.create(name, color, "folder")
                }
            }
        }
    }

    fun createNotebook(name: String, colorHex: String) {
        viewModelScope.launch { repository.create(name, colorHex, "folder") }
    }

    fun togglePin(id: String, pinned: Boolean) {
        viewModelScope.launch { repository.setPinned(id, pinned) }
    }

    fun archive(id: String) {
        viewModelScope.launch { repository.setArchived(id, true) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }
}
