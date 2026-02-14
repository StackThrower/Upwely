package com.warehouse.upwely.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.warehouse.upwely.data.AcumaticaApi
import com.warehouse.upwely.data.ShippingBox
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "PickingScanViewModel"

data class ScannedItem(
    val barcode: String,
    val itemName: String,
    val quantity: Int,
    val selectedBox: ShippingBox? = null,
    val serialNumber: String? = null,
)

data class PickingScanUiState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val isScanningSerialNumber: Boolean = false,
    val editingSerialNumberIndex: Int? = null,
    val scannedItems: List<ScannedItem> = emptyList(),
    val shippingBoxes: List<ShippingBox> = emptyList(),
    val requiredItems: List<RequiredItem> = emptyList(),
    val error: String? = null,
    val showBoxSelector: Boolean = false,
    val pendingBarcode: String? = null,
    val allItemsScanned: Boolean = false,
)

data class RequiredItem(
    val itemId: String,
    val name: String,
    val sku: String,
    val quantity: Int,
    val scannedQuantity: Int = 0,
)

class PickingScanViewModel(application: Application) : AndroidViewModel(application) {
    private val api = AcumaticaApi(application)

    private val _uiState = MutableStateFlow(PickingScanUiState())
    val uiState: StateFlow<PickingScanUiState> = _uiState.asStateFlow()

    fun initialize(items: List<RequiredItem>) {
        _uiState.value = _uiState.value.copy(requiredItems = items)
        loadShippingBoxes()
    }

    private fun loadShippingBoxes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            api.getShippingBoxes()
                .onSuccess { boxes ->
                    _uiState.value = _uiState.value.copy(
                        shippingBoxes = boxes,
                        isLoading = false,
                        error = null,
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message,
                    )
                }
        }
    }

    fun startScanning() {
        _uiState.value = _uiState.value.copy(isScanning = true)
    }

    fun stopScanning() {
        _uiState.value = _uiState.value.copy(
            isScanning = false,
            isScanningSerialNumber = false,
            editingSerialNumberIndex = null,
        )
    }

    fun startScanningSerialNumber(index: Int) {
        _uiState.value = _uiState.value.copy(
            isScanning = true,
            isScanningSerialNumber = true,
            editingSerialNumberIndex = index,
        )
    }

    fun onBarcodeScanned(barcode: String) {
        // Get the LATEST state to avoid race conditions
        val currentState = _uiState.value

        Log.d(TAG, "onBarcodeScanned: barcode=$barcode, isScanningSerialNumber=${currentState.isScanningSerialNumber}, editingIndex=${currentState.editingSerialNumberIndex}")

        // Check if we're scanning a serial number for an existing item
        if (currentState.isScanningSerialNumber && currentState.editingSerialNumberIndex != null) {
            val index = currentState.editingSerialNumberIndex
            if (index in currentState.scannedItems.indices) {
                val updatedScannedItems = currentState.scannedItems.toMutableList()
                updatedScannedItems[index] = updatedScannedItems[index].copy(serialNumber = barcode)

                _uiState.value = currentState.copy(
                    scannedItems = updatedScannedItems,
                    isScanning = false,
                    isScanningSerialNumber = false,
                    editingSerialNumberIndex = null,
                    error = null,
                )
                Log.d(TAG, "Serial number updated for index $index: $barcode")
            }
            return
        }

        // Check if this barcode matches any required item
        val matchingItem = currentState.requiredItems.find {
            it.sku == barcode || it.itemId == barcode
        }

        if (matchingItem != null) {
            // Show box selector for this item
            _uiState.value = currentState.copy(
                isScanning = false,
                showBoxSelector = true,
                pendingBarcode = barcode,
            )
        } else {
            // Barcode not found in required items
            _uiState.value = currentState.copy(
                error = "Item not found: $barcode",
            )
        }
    }

    fun onSerialNumberScanned(barcode: String, itemIndex: Int) {
        val currentState = _uiState.value

        if (itemIndex in currentState.scannedItems.indices) {
            val updatedScannedItems = currentState.scannedItems.toMutableList()
            updatedScannedItems[itemIndex] = updatedScannedItems[itemIndex].copy(serialNumber = barcode)

            _uiState.value = currentState.copy(
                scannedItems = updatedScannedItems,
                isScanning = false,
                isScanningSerialNumber = false,
                editingSerialNumberIndex = null,
                error = null,
            )
            Log.d(TAG, "Serial number set for index $itemIndex: $barcode")
        }
    }

    fun selectBox(box: ShippingBox) {
        val currentState = _uiState.value
        val barcode = currentState.pendingBarcode ?: return

        val matchingItem = currentState.requiredItems.find {
            it.sku == barcode || it.itemId == barcode
        } ?: return

        // Add scanned item
        val newScannedItem = ScannedItem(
            barcode = barcode,
            itemName = matchingItem.name,
            quantity = 1,
            selectedBox = box,
            serialNumber = barcode,
        )

        val updatedScannedItems = currentState.scannedItems + newScannedItem

        // Update required items with scanned quantity
        val updatedRequiredItems = currentState.requiredItems.map { item ->
            if (item.sku == barcode || item.itemId == barcode) {
                item.copy(scannedQuantity = item.scannedQuantity + 1)
            } else {
                item
            }
        }

        // Check if all items are scanned
        val allScanned = updatedRequiredItems.all { it.scannedQuantity >= it.quantity }

        _uiState.value = currentState.copy(
            scannedItems = updatedScannedItems,
            requiredItems = updatedRequiredItems,
            showBoxSelector = false,
            pendingBarcode = null,
            allItemsScanned = allScanned,
            error = null,
        )
    }

    fun dismissBoxSelector() {
        _uiState.value = _uiState.value.copy(
            showBoxSelector = false,
            pendingBarcode = null,
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retryLoadBoxes() {
        loadShippingBoxes()
    }

    fun getTotalScanned(): Int {
        return _uiState.value.scannedItems.size
    }

    fun getTotalRequired(): Int {
        return _uiState.value.requiredItems.sumOf { it.quantity }
    }
}
