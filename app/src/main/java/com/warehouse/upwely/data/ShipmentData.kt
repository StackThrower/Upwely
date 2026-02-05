package com.warehouse.upwely.data

import android.content.Context
import org.json.JSONObject
import java.io.IOException

data class PickupItemConfig(
    val id: String,
    val name: String,
    val sku: String,
    val quantity: Int,
    val shelfId: String,
)

data class ShipmentConfig(
    val id: String,
    val destination: String,
    val status: String,
    val items: List<PickupItemConfig>,
)

data class ShipmentsData(
    val shipments: List<ShipmentConfig>,
)

fun loadShipmentsConfig(context: Context): ShipmentsData {
    val json = try {
        context.assets.open("shipments_config.json").bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        return ShipmentsData(emptyList())
    }
    return parseShipmentsConfig(json)
}

fun parseShipmentsConfig(json: String): ShipmentsData {
    val obj = JSONObject(json)
    val shipmentsArr = obj.optJSONArray("shipments") ?: return ShipmentsData(emptyList())

    val shipments = buildList {
        for (i in 0 until shipmentsArr.length()) {
            val s = shipmentsArr.getJSONObject(i)
            val itemsArr = s.optJSONArray("items")
            val items = buildList {
                if (itemsArr != null) {
                    for (j in 0 until itemsArr.length()) {
                        val item = itemsArr.getJSONObject(j)
                        add(
                            PickupItemConfig(
                                id = item.getString("id"),
                                name = item.getString("name"),
                                sku = item.getString("sku"),
                                quantity = item.getInt("quantity"),
                                shelfId = item.getString("shelfId"),
                            )
                        )
                    }
                }
            }
            add(
                ShipmentConfig(
                    id = s.getString("id"),
                    destination = s.getString("destination"),
                    status = s.optString("status", "pending"),
                    items = items,
                )
            )
        }
    }
    return ShipmentsData(shipments)
}
