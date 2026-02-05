package com.warehouse.upwely.ui.screens

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.warehouse.upwely.data.PlanDoor
import com.warehouse.upwely.data.PlanRoom
import com.warehouse.upwely.data.PlanShelf
import com.warehouse.upwely.data.PointSequence
import com.warehouse.upwely.data.WarehousePlan
import com.warehouse.upwely.data.loadWarehousePlan
import com.warehouse.upwely.data.saveWarehousePlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class EditorMode { SELECT, ADD_ROOM, ADD_DOOR, ADD_SHELF }

sealed class SelectedElement {
    data class Room(val index: Int) : SelectedElement()
    data class Door(val index: Int) : SelectedElement()
    data class Shelf(val index: Int) : SelectedElement()
}

class PlanEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _rooms = MutableStateFlow<List<PlanRoom>>(emptyList())
    val rooms: StateFlow<List<PlanRoom>> = _rooms

    private val _doors = MutableStateFlow<List<PlanDoor>>(emptyList())
    val doors: StateFlow<List<PlanDoor>> = _doors

    private val _shelves = MutableStateFlow<List<PlanShelf>>(emptyList())
    val shelves: StateFlow<List<PlanShelf>> = _shelves

    private val _editorMode = MutableStateFlow(EditorMode.SELECT)
    val editorMode: StateFlow<EditorMode> = _editorMode

    private val _selectedElement = MutableStateFlow<SelectedElement?>(null)
    val selectedElement: StateFlow<SelectedElement?> = _selectedElement

    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> = _isDirty

    private var sequences: List<PointSequence> = emptyList()
    private var planWidth: Float = 20f
    private var planHeight: Float = 10f

    init {
        loadPlan()
    }

    private fun loadPlan() {
        val context = getApplication<Application>()
        val plan = loadWarehousePlan(context)
        _rooms.value = plan.rooms
        _doors.value = plan.doors
        _shelves.value = plan.shelves
        sequences = plan.sequences
        planWidth = plan.widthMeters
        planHeight = plan.heightMeters
        _isDirty.value = false
    }

    fun savePlan() {
        val context = getApplication<Application>()
        val plan = WarehousePlan(
            rooms = _rooms.value,
            doors = _doors.value,
            beacons = emptyList(),
            shelves = _shelves.value,
            widthMeters = planWidth,
            heightMeters = planHeight,
            sequences = sequences,
        )
        saveWarehousePlan(context, plan)
        _isDirty.value = false
    }

    fun setMode(mode: EditorMode) {
        _editorMode.value = mode
        if (mode != EditorMode.SELECT) {
            _selectedElement.value = null
        }
    }

    fun selectElement(element: SelectedElement?) {
        _selectedElement.value = element
    }

    fun handleTap(meterX: Float, meterY: Float) {
        when (_editorMode.value) {
            EditorMode.SELECT -> {
                _selectedElement.value = hitTest(meterX, meterY)
            }
            EditorMode.ADD_ROOM -> {
                addRoom(meterX, meterY)
                _editorMode.value = EditorMode.SELECT
            }
            EditorMode.ADD_DOOR -> {
                addDoor(meterX, meterY)
                _editorMode.value = EditorMode.SELECT
            }
            EditorMode.ADD_SHELF -> {
                addShelf(meterX, meterY)
                _editorMode.value = EditorMode.SELECT
            }
        }
    }

    fun moveElement(dx: Float, dy: Float) {
        val sel = _selectedElement.value ?: return
        when (sel) {
            is SelectedElement.Room -> {
                val list = _rooms.value.toMutableList()
                val room = list[sel.index]
                list[sel.index] = room.copy(x = room.x + dx, y = room.y + dy)
                _rooms.value = list
            }
            is SelectedElement.Door -> {
                val list = _doors.value.toMutableList()
                val door = list[sel.index]
                list[sel.index] = door.copy(x = door.x + dx, y = door.y + dy)
                _doors.value = list
            }
            is SelectedElement.Shelf -> {
                val list = _shelves.value.toMutableList()
                val shelf = list[sel.index]
                list[sel.index] = shelf.copy(
                    x = shelf.x + dx,
                    y = shelf.y + dy,
                    rectX = shelf.rectX + dx,
                    rectY = shelf.rectY + dy,
                )
                _shelves.value = list
            }
        }
        _isDirty.value = true
    }

    fun deleteSelected() {
        val sel = _selectedElement.value ?: return
        when (sel) {
            is SelectedElement.Room -> {
                _rooms.value = _rooms.value.toMutableList().apply { removeAt(sel.index) }
            }
            is SelectedElement.Door -> {
                _doors.value = _doors.value.toMutableList().apply { removeAt(sel.index) }
            }
            is SelectedElement.Shelf -> {
                _shelves.value = _shelves.value.toMutableList().apply { removeAt(sel.index) }
            }
        }
        _selectedElement.value = null
        _isDirty.value = true
    }

    fun updateRoom(index: Int, room: PlanRoom) {
        val list = _rooms.value.toMutableList()
        list[index] = room
        _rooms.value = list
        _isDirty.value = true
    }

    fun updateDoor(index: Int, door: PlanDoor) {
        val list = _doors.value.toMutableList()
        list[index] = door
        _doors.value = list
        _isDirty.value = true
    }

    fun updateShelf(index: Int, shelf: PlanShelf) {
        val list = _shelves.value.toMutableList()
        list[index] = shelf
        _shelves.value = list
        _isDirty.value = true
    }

    private fun addRoom(x: Float, y: Float) {
        val name = "Room ${_rooms.value.size + 1}"
        val room = PlanRoom(
            name = name,
            x = x - 1f, y = y - 1f,
            width = 2f, height = 2f,
            color = Color(0xFFB3C6FF),
        )
        _rooms.value = _rooms.value + room
        _selectedElement.value = SelectedElement.Room(_rooms.value.size - 1)
        _isDirty.value = true
    }

    private fun addDoor(x: Float, y: Float) {
        val roomA = findRoomAt(x, y) ?: "unknown"
        val roomB = "unknown"
        val door = PlanDoor(
            id = "d${_doors.value.size + 1}",
            roomA = roomA,
            roomB = roomB,
            x = x, y = y,
        )
        _doors.value = _doors.value + door
        _selectedElement.value = SelectedElement.Door(_doors.value.size - 1)
        _isDirty.value = true
    }

    private fun addShelf(x: Float, y: Float) {
        val room = findRoomAt(x, y) ?: "unknown"
        val shelf = PlanShelf(
            id = "shelf_${_shelves.value.size + 1}",
            x = x, y = y,
            room = room,
            rectX = x - 0.5f, rectY = y - 0.5f,
            rectWidth = 1f, rectHeight = 1f,
        )
        _shelves.value = _shelves.value + shelf
        _selectedElement.value = SelectedElement.Shelf(_shelves.value.size - 1)
        _isDirty.value = true
    }

    private fun findRoomAt(x: Float, y: Float): String? {
        return _rooms.value.find { r ->
            x >= r.x && x <= r.x + r.width &&
                y >= r.y && y <= r.y + r.height
        }?.name
    }

    private fun hitTest(x: Float, y: Float): SelectedElement? {
        // Test shelves first (smallest elements, highest priority)
        _shelves.value.forEachIndexed { i, shelf ->
            if (x >= shelf.rectX - 0.3f && x <= shelf.rectX + shelf.rectWidth + 0.3f &&
                y >= shelf.rectY - 0.3f && y <= shelf.rectY + shelf.rectHeight + 0.3f
            ) {
                return SelectedElement.Shelf(i)
            }
        }
        // Test doors (point proximity)
        _doors.value.forEachIndexed { i, door ->
            val dx = x - door.x
            val dy = y - door.y
            if (dx * dx + dy * dy <= 0.5f * 0.5f) {
                return SelectedElement.Door(i)
            }
        }
        // Test rooms
        _rooms.value.forEachIndexed { i, room ->
            if (x >= room.x && x <= room.x + room.width &&
                y >= room.y && y <= room.y + room.height
            ) {
                return SelectedElement.Room(i)
            }
        }
        return null
    }
}
