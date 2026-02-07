package com.warehouse.upwely.navigation

object Screen {
    const val PICKUP = "pickup"
    const val RECEIVE = "receive"
    const val WAREHOUSE_SELECTION = "warehouse_selection"
    const val LANGUAGE_SELECTION = "language_selection"
    const val ADD_WAREHOUSE = "add_warehouse"
    const val USER_PROFILE = "user_profile"
    const val EDIT_PROFILE = "edit_profile"
    const val WAREHOUSE_PLANNING = "warehouse_planning"
    const val ORDERS = "orders"
    const val SHIPMENTS = "shipments"
    const val CALIBRATION = "calibration"
    const val PICKING_MAP = "picking_map/{shipmentIds}"
    const val PLACING_MAP = "placing_map/{orderIds}"
    const val SHIPMENT_DETAIL = "shipment_detail/{shipmentId}"

    fun shipmentDetailRoute(shipmentId: String): String {
        return "shipment_detail/$shipmentId"
    }

    fun pickingMapRoute(shipmentIds: List<String>): String {
        return "picking_map/${shipmentIds.joinToString(",")}"
    }

    fun placingMapRoute(orderIds: List<String>): String {
        return "placing_map/${orderIds.joinToString(",")}"
    }
}
