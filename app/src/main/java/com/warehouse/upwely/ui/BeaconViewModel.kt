package com.warehouse.upwely.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.warehouse.upwely.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region

class BeaconViewModel(application: Application) : AndroidViewModel(application) {

    private val beaconManager = BeaconManager.getInstanceForApplication(application)
    private val rssiWindowMs = 13_000L
    private val beaconRssiHistory: MutableMap<String, MutableList<Pair<Long, Int>>> = mutableMapOf()

    private val _position = MutableStateFlow<Pair<Float, Float>?>(null)
    val position: StateFlow<Pair<Float, Float>?> = _position.asStateFlow()

    private val _currentRssi = MutableStateFlow<Map<String, Int>>(emptyMap())
    val currentRssi: StateFlow<Map<String, Int>> = _currentRssi.asStateFlow()

    private val _calibrationPoints = MutableStateFlow<List<CalibrationPoint>>(emptyList())
    val calibrationPoints: StateFlow<List<CalibrationPoint>> = _calibrationPoints.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT)
        )
        beaconManager.foregroundScanPeriod = 1000L
        beaconManager.foregroundBetweenScanPeriod = 200L

        beaconManager.addRangeNotifier { beacons, _ ->
            val now = System.currentTimeMillis()
            for (beacon in beacons) {
                val id = beacon.id1.toString()
                val history = beaconRssiHistory.getOrPut(id) { mutableListOf() }
                history.add(now to beacon.rssi)
            }

            cleanupOldHistory()

            // Update current average RSSI per beacon
            val rssiMap = beaconRssiHistory.mapValues { (_, list) ->
                list.map { it.second }.average().toInt()
            }
            _currentRssi.value = rssiMap

            // Calculate position from calibration map
            val calibration = _calibrationPoints.value
            if (calibration.isNotEmpty() && beaconRssiHistory.isNotEmpty()) {
                val pos = weightedAverageCalibrationPointWithHistory(
                    calibration, beaconRssiHistory, k = 2
                )
                if (pos != null) {
                    _position.value = pos
                }
            }
        }

        loadCalibration()
    }

    fun startScanning() {
        try {
            beaconManager.startRangingBeacons(Region("all-beacons", null, null, null))
            _isScanning.value = true
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    fun stopScanning() {
        try {
            beaconManager.stopRangingBeacons(Region("all-beacons", null, null, null))
            _isScanning.value = false
        } catch (_: Exception) {
        }
    }

    fun addCalibrationPoint(x: Float, y: Float) {
        val rssi = _currentRssi.value
        if (rssi.isEmpty()) return
        val point = CalibrationPoint(x, y, rssi)
        val updated = _calibrationPoints.value + point
        _calibrationPoints.value = updated
        saveCalibrationMap(getApplication(), updated)
    }

    fun clearCalibration() {
        _calibrationPoints.value = emptyList()
        clearCalibrationMap(getApplication())
    }

    private fun loadCalibration() {
        _calibrationPoints.value = loadCalibrationMap(getApplication())
    }

    private fun cleanupOldHistory() {
        val now = System.currentTimeMillis()
        val iter = beaconRssiHistory.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            entry.value.removeAll { now - it.first > rssiWindowMs }
            if (entry.value.isEmpty()) iter.remove()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
        beaconRssiHistory.clear()
    }
}
