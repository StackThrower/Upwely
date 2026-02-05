package com.warehouse.upwely.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import java.io.IOException

data class PlanRoom(
    val name: String,
    val x: Float, val y: Float,
    val width: Float, val height: Float,
    val color: Color,
)

data class PlanDoor(
    val id: String,
    val roomA: String, val roomB: String,
    val x: Float, val y: Float,
)

data class PlanBeacon(val id: String, val x: Float, val y: Float)

data class PlanShelf(
    val id: String,
    val x: Float, val y: Float,
    val room: String,
    val rectX: Float, val rectY: Float,
    val rectWidth: Float, val rectHeight: Float,
    val row: Int? = null,
    val cell: Int? = null,
)

data class PointSequence(
    val id: String,
    val pointIds: List<String>,
    val deliveryIds: List<String>,
)

data class WarehousePlan(
    val rooms: List<PlanRoom>,
    val doors: List<PlanDoor>,
    val beacons: List<PlanBeacon>,
    val shelves: List<PlanShelf>,
    val widthMeters: Float,
    val heightMeters: Float,
    val sequences: List<PointSequence>,
)

fun loadWarehousePlan(context: Context): WarehousePlan {
    val json = try {
        context.assets.open("flat_plan.json").bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        return WarehousePlan(emptyList(), emptyList(), emptyList(), emptyList(), 30f, 22f, emptyList())
    }
    return parseWarehousePlan(json)
}

fun parseWarehousePlan(json: String): WarehousePlan {
    val obj = JSONObject(json)
    val planWidth = obj.optDouble("planWidthMeters", 30.0).toFloat()
    val planHeight = obj.optDouble("planHeightMeters", 22.0).toFloat()

    val rooms = buildList {
        obj.optJSONArray("rooms")?.let { arr ->
            for (i in 0 until arr.length()) {
                val r = arr.getJSONObject(i)
                add(PlanRoom(
                    name = r.getString("name"),
                    x = r.getDouble("x").toFloat(),
                    y = r.getDouble("y").toFloat(),
                    width = r.getDouble("width").toFloat(),
                    height = r.getDouble("height").toFloat(),
                    color = Color(android.graphics.Color.parseColor(r.getString("color"))),
                ))
            }
        }
    }

    val doors = buildList {
        obj.optJSONArray("doors")?.let { arr ->
            for (i in 0 until arr.length()) {
                val d = arr.getJSONObject(i)
                add(PlanDoor(
                    id = d.getString("id"),
                    roomA = d.getString("roomA"),
                    roomB = d.getString("roomB"),
                    x = d.getDouble("x").toFloat(),
                    y = d.getDouble("y").toFloat(),
                ))
            }
        }
    }

    val beacons = buildList {
        obj.optJSONArray("beacon_positions")?.let { arr ->
            for (i in 0 until arr.length()) {
                val b = arr.getJSONObject(i)
                add(PlanBeacon(
                    id = b.getString("id"),
                    x = b.getDouble("x").toFloat(),
                    y = b.getDouble("y").toFloat(),
                ))
            }
        }
    }

    val shelves = buildList {
        obj.optJSONArray("random_points")?.let { arr ->
            for (i in 0 until arr.length()) {
                val rp = arr.getJSONObject(i)
                add(PlanShelf(
                    id = rp.getString("id"),
                    x = rp.getDouble("x").toFloat(),
                    y = rp.getDouble("y").toFloat(),
                    room = rp.getString("room"),
                    rectX = rp.getDouble("rect_x").toFloat(),
                    rectY = rp.getDouble("rect_y").toFloat(),
                    rectWidth = rp.getDouble("rect_width").toFloat(),
                    rectHeight = rp.getDouble("rect_height").toFloat(),
                    row = if (rp.has("row")) rp.getInt("row") else null,
                    cell = if (rp.has("cell")) rp.getInt("cell") else null,
                ))
            }
        }
    }

    val sequences = buildList {
        obj.optJSONArray("random_point_sequences")?.let { arr ->
            for (i in 0 until arr.length()) {
                val seq = arr.getJSONObject(i)
                val pointIds = buildList {
                    seq.getJSONArray("point_ids").let { pArr ->
                        for (j in 0 until pArr.length()) add(pArr.getString(j))
                    }
                }
                val deliveryIds = buildList {
                    seq.optJSONArray("delivery_ids")?.let { dArr ->
                        for (j in 0 until dArr.length()) add(dArr.getString(j))
                    }
                }
                add(PointSequence(seq.getString("sequence_id"), pointIds, deliveryIds))
            }
        }
    }

    return WarehousePlan(rooms, doors, beacons, shelves, planWidth, planHeight, sequences)
}
