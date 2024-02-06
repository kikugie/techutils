package dev.kikugie.techutils.util.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import java.util.*
import kotlin.math.max

class ScissorStack {
    private val stack: Stack<ScreenRect> = Stack()

    /**
     * Pushes a new screen rectangle onto the stack and sets it as the current scissor region.
     *
     * @param x1 Top-left corner of the rectangle.
     * @param y1 Top-left corner of the rectangle.
     * @param x2 Bottom-right corner of the rectangle.
     * @param y2 Bottom-right corner of the rectangle.
     * @return The pushed ScreenRect object.
     */
    fun push(x1: Int, y1: Int, x2: Int, y2: Int): ScreenRect {
        val rect = ScreenRect.ofCorners(x1, y1, x2, y2)
        val new = stack.lastOrNull()?.intersection(rect) ?: rect

        setScissor(new)
        return stack.push(new)
    }
    fun pop(): ScreenRect? {
        val rect = if (stack.empty())
            throw IllegalStateException("Scissor stack underflow")
        else {
            stack.pop()
            if (stack.empty()) null else stack.peek()
        }
        setScissor(rect)
        return rect
    }

    private fun setScissor(rect: ScreenRect?) {
        if (rect != null && !rect.empty) {
            val window = MinecraftClient.getInstance().window
            val i = window.framebufferHeight
            val d = window.scaleFactor
            val e: Double = rect.x.toDouble() * d
            val f: Double = i.toDouble() - rect.y1.toDouble() * d
            val g: Double = rect.width.toDouble() * d
            val h: Double = rect.height.toDouble() * d
            RenderSystem.enableScissor(
                e.toInt(), f.toInt(),
                max(0.0, g.toInt().toDouble()).toInt(), max(0.0, h.toInt().toDouble()).toInt()
            )
        } else {
            RenderSystem.disableScissor()
        }
    }
}