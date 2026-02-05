package com.warehouse.upwely.data

import android.content.Context
import org.json.JSONObject
import java.io.IOException

data class OrderItemConfig(
    val id: String,
    val name: String,
    val sku: String,
    val quantity: Int,
    val shelfId: String,
)

data class OrderConfig(
    val id: String,
    val supplier: String,
    val status: String,
    val items: List<OrderItemConfig>,
)

data class OrdersData(
    val orders: List<OrderConfig>,
)

fun loadOrdersConfig(context: Context): OrdersData {
    val json = try {
        context.assets.open("orders_config.json").bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        return OrdersData(emptyList())
    }
    return parseOrdersConfig(json)
}

fun parseOrdersConfig(json: String): OrdersData {
    val obj = JSONObject(json)
    val ordersArr = obj.optJSONArray("orders") ?: return OrdersData(emptyList())

    val orders = buildList {
        for (i in 0 until ordersArr.length()) {
            val o = ordersArr.getJSONObject(i)
            val itemsArr = o.optJSONArray("items")
            val items = buildList {
                if (itemsArr != null) {
                    for (j in 0 until itemsArr.length()) {
                        val item = itemsArr.getJSONObject(j)
                        add(
                            OrderItemConfig(
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
                OrderConfig(
                    id = o.getString("id"),
                    supplier = o.getString("supplier"),
                    status = o.optString("status", "new"),
                    items = items,
                )
            )
        }
    }
    return OrdersData(orders)
}
