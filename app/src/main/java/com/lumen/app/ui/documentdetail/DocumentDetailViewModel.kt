package com.lumen.app.ui.documentdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.app.domain.model.LumenDocument
import com.lumen.app.domain.model.OcrResult
import com.lumen.app.domain.repository.DocumentRepository
import com.lumen.app.domain.repository.OcrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val documentRepository: DocumentRepository,
    private val ocrRepository: OcrRepository,
) : ViewModel() {

    private val documentId: String = checkNotNull(savedStateHandle["documentId"])

    val document: StateFlow<LumenDocument?> = documentRepository.observeById(documentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val ocrResult: StateFlow<OcrResult?> = document
        .flatMapLatest { doc ->
            val ocrId = doc?.pages?.firstOrNull()?.ocrResultId
            if (ocrId != null) ocrRepository.observeResult(ocrId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
