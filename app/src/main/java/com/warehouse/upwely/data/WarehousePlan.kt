package com.warehouse.upwely.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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

private const val PLAN_FILE_NAME = "warehouse_plan.json"

fun loadWarehousePlan(context: Context): WarehousePlan {
    val file = File(context.filesDir, PLAN_FILE_NAME)
    val json = try {
        if (file.exists()) {
            file.readText()
        } else {
            context.assets.open("flat_plan.json").bufferedReader().use { it.readText() }
        }
    } catch (e: IOException) {
        return WarehousePlan(emptyList(), emptyList(), emptyList(), emptyList(), 30f, 22f, emptyList())
    }
    return parseWarehousePlan(json)
}

fun saveWarehousePlan(context: Context, plan: WarehousePlan) {
    val json = serializeWarehousePlan(plan)
    File(context.filesDir, PLAN_FILE_NAME).writeText(json)
}

fun serializeWarehousePlan(plan: WarehousePlan): String {
    val obj = JSONObject()

    obj.put("planWidthMeters", plan.widthMeters.toDouble())
    obj.put("planHeightMeters", plan.heightMeters.toDouble())

    obj.put("rooms", JSONArray().apply {
        for (room in plan.rooms) {
            put(JSONObject().apply {
                put("name", room.name)
                put("x", room.x.toDouble())
                put("y", room.y.toDouble())
                put("width", room.width.toDouble())
                put("height", room.height.toDouble())
                val argb = room.color.toArgb()
                put("color", String.format("#%02X%02X%02X",
                    (argb shr 16) and 0xFF,
                    (argb shr 8) and 0xFF,
                    argb and 0xFF))
            })
        }
    })

    obj.put("doors", JSONArray().apply {
        for (door in plan.doors) {
            put(JSONObject().apply {
                put("id", door.id)
                put("roomA", door.roomA)
                put("roomB", door.roomB)
                put("x", door.x.toDouble())
                put("y", door.y.toDouble())
            })
        }
    })

    obj.put("random_points", JSONArray().apply {
        for (shelf in plan.shelves) {
            put(JSONObject().apply {
                put("id", shelf.id)
                put("x", shelf.x.toDouble())
                put("y", shelf.y.toDouble())
                put("room", shelf.room)
                put("rect_x", shelf.rectX.toDouble())
                put("rect_y", shelf.rectY.toDouble())
                put("rect_width", shelf.rectWidth.toDouble())
                put("rect_height", shelf.rectHeight.toDouble())
                shelf.row?.let { put("row", it) }
                shelf.cell?.let { put("cell", it) }
            })
        }
    })

    obj.put("beacon_positions", JSONArray().apply {
        for (beacon in plan.beacons) {
            put(JSONObject().apply {
                put("id", beacon.id)
                put("x", beacon.x.toDouble())
                put("y", beacon.y.toDouble())
            })
        }
    })

    obj.put("random_point_sequences", JSONArray().apply {
        for (seq in plan.sequences) {
            put(JSONObject().apply {
                put("sequence_id", seq.id)
                put("point_ids", JSONArray(seq.pointIds))
                put("delivery_ids", JSONArray(seq.deliveryIds))
            })
        }
    })

    return obj.toString(2)
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
