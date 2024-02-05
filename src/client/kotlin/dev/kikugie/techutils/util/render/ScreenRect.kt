package dev.kikugie.techutils.util.render

import net.minecraft.client.MinecraftClient
import kotlin.math.max
import kotlin.math.min

data class ScreenRect(val x: Int, val y: Int, val width: Int, val height: Int) {
    val x1 = x + width
    val y1 = x + height
    val empty = height == 0 || width == 0
    val centerX = x + width / 2
    val centerY = y + height / 2

    init {
        require(width >= 0 && height >= 0) { "Must have non-negative dimensions: $width, $height" }
    }

    /**
     * Checks if the current [ScreenRect] is fully contained within the given [ScreenRect].
     *
     * @return `true` if there is an overlap, `false` otherwise.
     */
    fun overlaps(other: ScreenRect) = x <= other.x && y <= other.y && x1 >= other.x1 && y1 >= other.y1
    /**
     * Checks if the given coordinates fall within a specified range.
     *
     * @return `true` if the coordinates fall within the specified range, `false` otherwise.
     */
    fun contains(x: Int, y: Int) = x in x..x1 && y in y..y1
    /**
     * Shifts the current screen rectangle by the specified amounts in the X and Y directions.
     *
     * @return A new [ScreenRect] with the shifted coordinates.
     */
    fun shift(dx: Int = 0, dy: Int = 0) = ScreenRect(x1 + dx, y1 + dy, width, height)
    /**
     * Expands the current screen rectangle by adding the specified margin values.
     *
     * @return A new [ScreenRect] object representing an expanded screen rectangle.
     */
    fun expand(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) =
        ScreenRect(x - left, y - top, width + right, height + bottom)
    fun shrink(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) = expand(-left, -top, -right, -bottom)
    fun shrink(direction: ScreenDirection, amount: Int) = expand(direction, -amount)
    /**
     * Expands the [ScreenRect] in the given direction by the specified amount.
     *
     * @return a new [ScreenRect] expanded in the specified direction by the specified amount
     */
    fun expand(direction: ScreenDirection, amount: Int) = when (direction) {
        ScreenDirection.UP -> expand(top = amount)
        ScreenDirection.DOWN -> expand(bottom = amount)
        ScreenDirection.LEFT -> expand(left = amount)
        ScreenDirection.RIGHT -> expand(right = amount)
    }

    /**
     * Calculates the intersection between two [ScreenRect] objects.
     *
     * @return The resulting intersection [ScreenRect] object. If there is no intersection, returns an empty [ScreenRect] object.
     */
    fun intersection(other: ScreenRect): ScreenRect {
        val i = max(x, other.x)
        val j = max(y, other.y)
        val k = min(x1, other.x1)
        val l = min(y1, other.y1)
        return if (i < k && j < l) ofCorners(i, j, k, l) else Companion.empty
    }


    /**
     * Creates a [ScreenRect] that fits this and the other rectangle.
     *
     * @return A new [ScreenRect] object representing the union of the two objects.
     */
    fun union(other: ScreenRect): ScreenRect {
        val i = min(x, other.x)
        val j = min(y, other.y)
        val k = max(x1, other.x1)
        val l = max(y1, other.y1)
        return ofCorners(i, j, k, l)
    }

    /**
     * Check if this rectangle intersects with the specified other screen rectangle.
     *
     * @return `true` if there is an intersection, `false` otherwise.
     */
    fun intersects(other: ScreenRect) = !intersection(other).empty

    companion object {
        /**
         * Represents an empty screen rectangle.
         *
         * @return [ScreenRect] with all coordinates initialized to zero.
         */
        val empty = ScreenRect(0,0,0,0)
        /**
         * Represents the current Minecraft window area.
         *
         * @return [ScreenRect] at 0, 0 with width and height set from the Minecraft window
         */
        val fullscreen: ScreenRect
            get() = MinecraftClient.getInstance().window.let { ScreenRect(0, 0, it.width, it.height) }
        /**
         * Creates a [ScreenRect] based on the given coordinates of the top left corner (x, y) and bottom right corner (x1, y1).
         *
         * @return [ScreenRect] representing the area between the two corners.
         */
        fun ofCorners(x: Int, y: Int, x1: Int, y1: Int) = ScreenRect(x, y, x1 - x, y1 - y)
    }
}
