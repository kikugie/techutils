package dev.kikugie.techutils.client.feature.litegui.browser

import dev.kikugie.techutils.client.feature.preview.PreviewConfig
import dev.kikugie.techutils.client.feature.preview.Structure
import dev.kikugie.techutils.client.feature.preview.render.StructureRenderable
import dev.kikugie.techutils.client.util.render.Colors
import dev.kikugie.techutils.client.util.render.ScissorStack
import fi.dy.masa.malilib.gui.widgets.WidgetBase
import fi.dy.masa.malilib.gui.widgets.WidgetContainer
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import fi.dy.masa.malilib.render.RenderUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.util.math.MathHelper
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sign

class MetadataWidget(
    private val structure: Structure
    // Dimensions are updated at render time
) : WidgetContainer(0, 0, 0, 0) {
    private val scissors = ScissorStack()
    private val model = ModelWidget(structure, scissors)
    var scroll = 0

    init {
        addWidget(model)
    }

    override fun onMouseScrolledImpl(
        mouseX: Int,
        mouseY: Int,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        scroll = MathHelper.clamp(scroll + sign(verticalAmount).toInt(), 0, structure.metadata.array.size - 1)
        return true
    }

    fun onMouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        deltaX: Double,
        deltaY: Double
    ): Boolean {
        return model.onMouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun render(mouseX: Int, mouseY: Int, selected: Boolean, context: DrawContext) {
        RenderUtils.drawOutlinedBox(x, y, width, height, Colors.guiBackground.intValue, Colors.guiBorder.intValue)
        scissors.pushDirect(x + 2, y + 2, width - 4, height - 4)
        drawMetadataWidget(mouseX, mouseY, selected, context)
        scissors.pop()
    }

    private fun drawMetadataWidget(mouseX: Int, mouseY: Int, selected: Boolean, context: DrawContext) {
        model.x = x + 4
        model.y = y + 4
        model.width = width - 8
        model.height = height - 8

        model.render(mouseX, mouseY, selected, context)
    }

    class ModelWidget(
        structure: Structure,
        scissors: ScissorStack
    ) : WidgetBase(0, 0, 0, 0) {
        private val renderable = StructureRenderable.from(structure, scissors)
        private val ready
            get() = renderable.mesh.canRender()
        private var dragging = false

        override fun render(mouseX: Int, mouseY: Int, selected: Boolean, context: DrawContext) {
            if (renderable.mesh.canRender())
                renderable.drawModel(x, y, width)

            RenderUtils.drawOutline(x, y, width, width, Colors.guiBorder.intValue)
        }

        override fun onMouseScrolledImpl(
            mouseX: Int,
            mouseY: Int,
            horizontalAmount: Double,
            verticalAmount: Double
        ): Boolean {
            if (!ready) return false
            val amount = horizontalAmount + verticalAmount
            val property = renderable.properties().scale
            val modifier = PreviewConfig.scaleSensitivity.doubleValue * 0.1
            val scale = property.get()
            val linear = ln(scale.toDouble() / 100)
            val newScale = Math.E.pow(linear + amount * modifier) * 100
            property.modify(newScale.toInt() - scale)
            return true
        }

        fun onMouseDragged(
            mouseX: Double,
            mouseY: Double,
            button: Int,
            deltaX: Double,
            deltaY: Double
        ): Boolean {
            if (!ready || !dragging)
                return false
            val modifier = PreviewConfig.rotationSensitivity.doubleValue * 5
            when (button) {
                0 -> {
                    renderable.properties().rotation.modify((deltaX * modifier).toInt())
                }

                1 -> {
                    renderable.shift(deltaX, deltaY)
                }
            }
            return true
        }

        override fun onMouseClickedImpl(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
            dragging = true
            return true
        }

        override fun onMouseReleasedImpl(mouseX: Int, mouseY: Int, mouseButton: Int) {
            dragging = false
        }
    }
}