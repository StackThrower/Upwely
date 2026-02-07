package com.warehouse.upwely.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warehouse.upwely.data.AcumaticaApi
import com.warehouse.upwely.data.ApiShipment
import com.warehouse.upwely.data.ShipmentsCache
import com.warehouse.upwely.data.toShipmentConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ShipmentsUiState {
    data object Loading : ShipmentsUiState()
    data class Success(val shipments: List<ApiShipment>) : ShipmentsUiState()
    data class Error(val message: String) : ShipmentsUiState()
}

class ShipmentsViewModel : ViewModel() {

    private val api = AcumaticaApi()

    private val _uiState = MutableStateFlow<ShipmentsUiState>(ShipmentsUiState.Loading)
    val uiState: StateFlow<ShipmentsUiState> = _uiState.asStateFlow()

    init {
        loadShipments()
    }

    fun loadShipments() {
        viewModelScope.launch {
            _uiState.value = ShipmentsUiState.Loading
            api.getOpenShipments()
                .onSuccess { shipments ->
                    // Cache shipments for PickingMapScreen
                    ShipmentsCache.cachedShipments = shipments.map { it.toShipmentConfig() }
                    _uiState.value = ShipmentsUiState.Success(shipments)
                }
                .onFailure { error ->
                    _uiState.value = ShipmentsUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}
