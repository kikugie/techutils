package dev.kikugie.techutils.gui.browser

import dev.kikugie.techutils.config.BrowserConfig
import dev.kikugie.techutils.gui.util.Colors
import dev.kikugie.techutils.gui.util.Draggable
import dev.kikugie.techutils.impl.structure.Structure
import dev.kikugie.techutils.util.render.ScissorStack
import dev.kikugie.worldrenderer.mesh.WorldMesh
import dev.kikugie.worldrenderer.property.DefaultRenderProperties
import dev.kikugie.worldrenderer.render.AreaRenderable
import dev.kikugie.worldrenderer.render.RenderableDispatcher
import fi.dy.masa.malilib.gui.widgets.WidgetBase
import fi.dy.masa.malilib.render.RenderUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.E
import kotlin.math.ln
import kotlin.math.pow

class ModelWidget(
    structure: Structure,
    private val scissorStack: ScissorStack,
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
        RenderUtils.drawOutline(x, y, width, height, 1, Colors.guiBorder.intValue)
        if (ready) drawModel()
    }

    private fun drawModel() {
        // FIXME: WHY THE FUCK ARE YOU NOT WORKING
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
        if (!isMouseOver(x.toInt(), y.toInt())) return true.also { dragging = false }
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