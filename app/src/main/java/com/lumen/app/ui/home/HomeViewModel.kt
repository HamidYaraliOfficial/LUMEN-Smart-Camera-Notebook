package com.lumen.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.app.domain.model.LumenDocument
import com.lumen.app.domain.model.LumenTask
import com.lumen.app.domain.model.Note
import com.lumen.app.domain.model.Receipt
import com.lumen.app.domain.repository.DocumentRepository
import com.lumen.app.domain.repository.NoteRepository
import com.lumen.app.domain.repository.ReceiptRepository
import com.lumen.app.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val recentDocuments: List<LumenDocument> = emptyList(),
    val recentNotes: List<Note> = emptyList(),
    val recentReceipts: List<Receipt> = emptyList(),
    val pendingTasks: List<LumenTask> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    documentRepository: DocumentRepository,
    noteRepository: NoteRepository,
    receiptRepository: ReceiptRepository,
    taskRepository: TaskRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        documentRepository.observeRecent(6),
        noteRepository.observeRecent(6),
        receiptRepository.observeRecent(6),
        taskRepository.observePendingTasks(),
    ) { documents, notes, receipts, tasks ->
        HomeUiState(documents, notes, receipts, tasks.take(6))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
