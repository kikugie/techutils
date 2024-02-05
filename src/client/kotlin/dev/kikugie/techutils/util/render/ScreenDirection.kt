package dev.kikugie.techutils.util.render

enum class ScreenDirection {
    UP, DOWN, LEFT, RIGHT;

    val opposite
        get() = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }
    val clockwise
        get() = when (this) {
            UP -> RIGHT
            DOWN -> LEFT
            LEFT -> UP
            RIGHT -> DOWN
        }
    val counterClockwise: ScreenDirection
        get() = clockwise.opposite
    val positive
        get() = when(this) {
            UP, LEFT -> false
            DOWN, RIGHT -> true
        }
    val horizontal
        get() = when(this) {
            UP, DOWN -> false
            LEFT, RIGHT -> true
        }
    val vertical = !horizontal
}