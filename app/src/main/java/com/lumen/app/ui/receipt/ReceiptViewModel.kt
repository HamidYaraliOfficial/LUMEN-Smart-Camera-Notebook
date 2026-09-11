package com.lumen.app.ui.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumen.app.domain.model.Receipt
import com.lumen.app.domain.repository.ReceiptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ReceiptRepository,
) : ViewModel() {
    private val receiptId: String = checkNotNull(savedStateHandle["receiptId"])

    val receipt: StateFlow<Receipt?> = repository.observeById(receiptId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateCategory(category: String) {
        val current = receipt.value ?: return
        viewModelScope.launch { repository.upsert(current.copy(category = category)) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch { repository.delete(receiptId); onDeleted() }
    }
}
