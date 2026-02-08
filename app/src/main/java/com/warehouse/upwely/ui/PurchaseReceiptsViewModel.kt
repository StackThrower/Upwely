package com.warehouse.upwely.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.warehouse.upwely.data.AcumaticaApi
import com.warehouse.upwely.data.ApiPurchaseReceipt
import com.warehouse.upwely.data.PurchaseReceiptsCache
import com.warehouse.upwely.data.toPurchaseReceiptConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PurchaseReceiptsUiState {
    data object Loading : PurchaseReceiptsUiState()
    data class Success(val receipts: List<ApiPurchaseReceipt>) : PurchaseReceiptsUiState()
    data class Error(val message: String) : PurchaseReceiptsUiState()
}

class PurchaseReceiptsViewModel(application: Application) : AndroidViewModel(application) {

    private val api = AcumaticaApi(application)

    private val _uiState = MutableStateFlow<PurchaseReceiptsUiState>(PurchaseReceiptsUiState.Loading)
    val uiState: StateFlow<PurchaseReceiptsUiState> = _uiState.asStateFlow()

    init {
        loadPurchaseReceipts()
    }

    fun loadPurchaseReceipts() {
        viewModelScope.launch {
            _uiState.value = PurchaseReceiptsUiState.Loading
            api.getPurchaseReceipts()
                .onSuccess { receipts ->
                    // Cache receipts for PlacingMapScreen
                    PurchaseReceiptsCache.cachedReceipts = receipts.map { it.toPurchaseReceiptConfig() }
                    _uiState.value = PurchaseReceiptsUiState.Success(receipts)
                }
                .onFailure { error ->
                    _uiState.value = PurchaseReceiptsUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}
