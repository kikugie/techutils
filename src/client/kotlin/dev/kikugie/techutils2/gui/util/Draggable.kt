package dev.kikugie.techutils2.gui.util

interface Draggable {
    fun onDragged(x: Double, y: Double, dx: Double, dy: Double, button: Int): Boolean
}