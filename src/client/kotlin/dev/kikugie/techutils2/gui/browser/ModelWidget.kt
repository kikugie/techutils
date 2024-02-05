package dev.kikugie.techutils2.gui.browser

import dev.kikugie.techutils2.util.render.ScissorStack
import dev.kikugie.techutils2.config.BrowserConfig
import dev.kikugie.techutils2.gui.util.Draggable
import dev.kikugie.techutils2.impl.structure.Structure
import dev.kikugie.worldrenderer.mesh.WorldMesh
import dev.kikugie.worldrenderer.property.DefaultRenderProperties
import dev.kikugie.worldrenderer.render.AreaRenderable
import dev.kikugie.worldrenderer.render.RenderableDispatcher
import fi.dy.masa.malilib.gui.widgets.WidgetBase
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.E
import kotlin.math.ln
import kotlin.math.pow

class ModelWidget(
    structure: Structure,
    private val scissorStack: ScissorStack
) : WidgetBase(0, 0, 0, 0), Draggable {
    private val client = MinecraftClient.getInstance()
    private val model = AreaRenderable(
        WorldMesh.create {
            world = structure.world
            origin = structure.world.origin
            end = structure.world.end
            entities = structure.world
        }.apply { scheduleRebuild() },
        DefaultRenderProperties(
            500.0,
            BrowserConfig.previewAngle.integerValue.toDouble(),
            BrowserConfig.previewSlant.integerValue.toDouble()
        ),
        { scissorStack.push(x, y, x + width, y + height) },
        { scissorStack.pop() }
    )
    private var dragging = false
    private val ready
        get() = model.mesh.canRender

    override fun render(mouseX: Int, mouseY: Int, selected: Boolean, context: MatrixStack) {
        if (ready) drawModel()
    }

    private fun drawModel() {
//        scissorStack.push(x, y, x + width, x + height)
        val window = client.window
        RenderableDispatcher.draw(
            model,
            window.framebufferWidth / window.framebufferHeight.toFloat(),
            client.tickDelta
        ) {
            it.move(
                x + model.properties.xOffset + width / 2,
                y + model.properties.yOffset + height / 2
            )
        }
//        scissorStack.pop()
    }

    override fun onMouseScrolledImpl(mouseX: Int, mouseY: Int, amount: Double): Boolean {
        if (ready) {
            val mod = BrowserConfig.mouseSensitivity.doubleValue
            model.properties.scale = E.pow(ln(model.properties.scale) + amount * mod)
        }
        return true
    }

    override fun onMouseClickedImpl(mouseX: Int, mouseY: Int, mouseButton: Int) = true.also { dragging = true }

    override fun onMouseReleasedImpl(mouseX: Int, mouseY: Int, mouseButton: Int) {
        dragging = false
    }

    override fun onDragged(x: Double, y: Double, dx: Double, dy: Double, button: Int): Boolean {
        if (!ready || !dragging) return true
        if (!isMouseOver(x.toInt(),y.toInt())) return true.also { dragging = false }
        val mod = BrowserConfig.mouseSensitivity.doubleValue * 5
        when (button) {
            0 -> model.properties.rotation += dx * mod
            1 -> {
                model.properties.xOffset += dx.toInt()
                model.properties.yOffset += dy.toInt()
            }
        }
        return true
    }

    private fun MatrixStack.move(x: Int, y: Int) {
        val screen = client.currentScreen!!
        val w = screen.width
        val h = screen.height
        translate((2F * x - w) / h, -(2F * y - h) / h, 0F)
    }
}