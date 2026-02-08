package com.warehouse.upwely.data

// API response models for PurchaseReceipt
data class ApiPurchaseReceiptDetail(
    val id: String,
    val inventoryId: String,
    val description: String,
    val receiptQty: Int,
    val location: String,
    val warehouse: String,
    val unitCost: Double,
    val extendedCost: Double,
    val poOrderNbr: String,
    val uom: String,
)

data class ApiPurchaseReceipt(
    val id: String,
    val receiptNbr: String,
    val vendorId: String,
    val vendorRef: String,
    val status: String,
    val date: String,
    val totalQty: Double,
    val totalCost: Double,
    val details: List<ApiPurchaseReceiptDetail>,
)

// UI models for PurchaseReceipt
data class PurchaseReceiptItemConfig(
    val id: String,
    val name: String,
    val sku: String,
    val quantity: Int,
    val location: String,
    val unitCost: Double,
    val extendedCost: Double,
    val poOrderNbr: String,
    val uom: String,
)

data class PurchaseReceiptConfig(
    val id: String,
    val receiptNbr: String,
    val vendorId: String,
    val vendorRef: String,
    val status: String,
    val date: String,
    val totalQty: Int,
    val totalCost: Double,
    val items: List<PurchaseReceiptItemConfig>,
)

fun ApiPurchaseReceipt.toPurchaseReceiptConfig(): PurchaseReceiptConfig {
    return PurchaseReceiptConfig(
        id = id,
        receiptNbr = receiptNbr,
        vendorId = vendorId,
        vendorRef = vendorRef,
        status = status,
        date = date,
        totalQty = totalQty.toInt(),
        totalCost = totalCost,
        items = details.map { detail ->
            PurchaseReceiptItemConfig(
                id = detail.id,
                name = detail.description.ifEmpty { detail.inventoryId },
                sku = detail.inventoryId,
                quantity = detail.receiptQty,
                location = detail.location,
                unitCost = detail.unitCost,
                extendedCost = detail.extendedCost,
                poOrderNbr = detail.poOrderNbr,
                uom = detail.uom,
            )
        },
    )
}

// Cache for PurchaseReceipts
object PurchaseReceiptsCache {
    var cachedReceipts: List<PurchaseReceiptConfig> = emptyList()
}
