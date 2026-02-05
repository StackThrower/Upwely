package com.warehouse.upwely.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class CalibrationPoint(
    val x: Float,
    val y: Float,
    val beacons: Map<String, Int>,
)

/**
 * Weighted average of k nearest calibration points in RSSI space.
 * Port of storagebeacon's weightedAverageCalibrationPoint.
 */
fun weightedAverageCalibrationPoint(
    calibrationPoints: List<CalibrationPoint>,
    currentRssi: Map<String, Int>,
    k: Int = 2,
): Pair<Float, Float>? {
    if (calibrationPoints.isEmpty() || currentRssi.isEmpty()) return null

    val distances = calibrationPoints.map { point ->
        var dist = 0f
        for ((beaconId, refRssi) in point.beacons) {
            val curr = currentRssi[beaconId] ?: continue
            dist += (curr - refRssi) * (curr - refRssi)
        }
        point to dist
    }.sortedBy { it.second }

    val nearest = distances.take(k).filter { it.second > 0f }
    if (nearest.isEmpty()) return null

    var sumWeights = 0f
    var avgX = 0f
    var avgY = 0f
    for ((point, dist) in nearest) {
        val weight = 1f / dist
        avgX += point.x * weight
        avgY += point.y * weight
        sumWeights += weight
    }
    return if (sumWeights > 0f) Pair(avgX / sumWeights, avgY / sumWeights) else null
}

/**
 * Positioning using RSSI history (averages per beacon over the time window).
 */
fun weightedAverageCalibrationPointWithHistory(
    calibrationPoints: List<CalibrationPoint>,
    rssiHistoryMap: Map<String, MutableList<Pair<Long, Int>>>,
    k: Int = 2,
): Pair<Float, Float>? {
    val avgRssiMap = rssiHistoryMap.mapValues { (_, list) ->
        list.map { it.second }.average().toInt()
    }
    return weightedAverageCalibrationPoint(calibrationPoints, avgRssiMap, k)
}

fun loadCalibrationMap(context: Context): List<CalibrationPoint> {
    val file = File(context.filesDir, "calibration_map.json")
    if (!file.exists()) return emptyList()
    return try {
        val arr = JSONArray(file.readText())
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val beaconsObj = obj.getJSONObject("beacons")
                val beacons = mutableMapOf<String, Int>()
                for (key in beaconsObj.keys()) {
                    beacons[key] = beaconsObj.getInt(key)
                }
                add(CalibrationPoint(
                    x = obj.getDouble("x").toFloat(),
                    y = obj.getDouble("y").toFloat(),
                    beacons = beacons,
                ))
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveCalibrationMap(context: Context, points: List<CalibrationPoint>) {
    val arr = JSONArray()
    for (p in points) {
        val obj = JSONObject()
        obj.put("x", p.x.toDouble())
        obj.put("y", p.y.toDouble())
        val beaconsObj = JSONObject()
        for ((id, rssi) in p.beacons) beaconsObj.put(id, rssi)
        obj.put("beacons", beaconsObj)
        arr.put(obj)
    }
    context.openFileOutput("calibration_map.json", Context.MODE_PRIVATE).use {
        it.write(arr.toString().toByteArray())
    }
}

fun clearCalibrationMap(context: Context) {
    val file = File(context.filesDir, "calibration_map.json")
    if (file.exists()) file.delete()
}
