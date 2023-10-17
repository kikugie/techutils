package dev.kikugie.techutils.client.util.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ScissorStack {
    private val stack: Deque<ScreenRect> = ArrayDeque()

    fun runDirect(x: Int, y: Int, width: Int, height: Int, action: () -> Unit) {
        run(ScreenRect.ofDims(x, y, width, height), action)
    }

    fun run(rect: ScreenRect, action: () -> Unit) {
        push(rect)
        enable()
        action()
        disable()
        pop()
    }

    fun pushDirect(x: Int, y: Int, width: Int, height: Int): ScreenRect {
        return push(ScreenRect.ofDims(x, y, width, height))
    }

    fun push(rect: ScreenRect): ScreenRect {
        val last = stack.peekLast()
        return if (last != null) {
            val new = rect.intersection(last)
            stack.addLast(new)
            new
        } else {
            stack.addLast(rect)
            rect
        }
    }

    fun pop(): ScreenRect {
        return if (stack.isEmpty()) {
            throw IllegalStateException("Scissor stack underflow")
        } else {
            stack.pollLast()
        }
    }

    fun peek(): ScreenRect? {
        return stack.peekLast()
    }

    fun enable() {
        val rect = stack.peekLast()
        if (rect != null) {
            val window = MinecraftClient.getInstance().window
            val i = window.framebufferHeight
            val d = window.scaleFactor
            val e: Double = rect.x1.toDouble() * d
            val f: Double = i.toDouble() - rect.y2.toDouble() * d
            val g: Double = rect.width.toDouble() * d
            val h: Double = rect.height.toDouble() * d
            RenderSystem.enableScissor(e.toInt(), f.toInt(), max(0, g.toInt()), max(0, h.toInt()))
        }
    }

    fun disable() {
        RenderSystem.disableScissor()
    }

    interface Provider {
        val scissorStack: ScissorStack
    }

    data class ScreenRect(
        val x1: Int,
        val y1: Int,
        val x2: Int,
        val y2: Int
    ) {
        val width
            get() = x2 - x1
        val height
            get() = y2 - y1

        fun intersection(other: ScreenRect): ScreenRect {
            val x1 = max(this.x1, other.x1)
            val y1 = max(this.y1, other.y1)
            val x2 = min(this.x2, other.x2)
            val y2 = min(this.y2, other.y2)
            return if (x1 < x2 && y1 < y2) ScreenRect(x1, y1, x2, y2) else empty
        }

        companion object {
            val empty = ScreenRect(0, 0, 0, 0)

            @JvmStatic
            fun ofDims(x: Int, y: Int, width: Int, height: Int): ScreenRect {
                return ScreenRect(x, y, x + width, y + height)
            }
        }
    }
}