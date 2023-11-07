package dev.kikugie.techutils.client.gui.browser

import dev.kikugie.techutils.client.feature.browser.BrowserConfig
import dev.kikugie.techutils.client.feature.browser.preview.StructureRenderable
import dev.kikugie.techutils.client.impl.structure.Structure
import dev.kikugie.techutils.client.util.multiversion.MetadataWidgetExtension
import dev.kikugie.techutils.client.util.multiversion.ModelWidgetExtension
import dev.kikugie.techutils.client.util.render.Colors
import dev.kikugie.techutils.client.util.render.ScissorStack
import dev.kikugie.techutils.client.util.render.TextUtils
import fi.dy.masa.malilib.render.RenderUtils
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.math.MathHelper
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sign

class MetadataWidget(
    private val structure: Structure
    // Dimensions are updated at render time
) : MetadataWidgetExtension() {
    private val scissors = ScissorStack()
    private val model = ModelWidget(structure, scissors)
    private var scroll = 0

    init {
        addWidget(model)
    }

    override fun onScrolled(x: Int, y: Int, amount: Double): Boolean {
        scroll = MathHelper.clamp(scroll + sign(amount).toInt(), 0, structure.metadata.map.size - 1)
        return model.onScrolled(x, y, amount)
    }

    override fun onDragged(x: Double, y: Double, dx: Double, dy: Double, button: Int) =
        model.onDragged(x, y, dx, dy, button)

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

        val lines = arrayListOf<String>()
        structure.metadata.map.forEach { (k, v) -> run { lines.addAll(formatLine(k, v, model.width)) } }

        model.render(mouseX, mouseY, selected, context)
    }

    private fun formatLine(key: String, value: String, width: Int): List<String> {
        val combined = "$key: $value"
        return if (TextUtils.isWithin(combined, width))
            listOf(StringUtils.translate(combined))
        else listOf(
            StringUtils.translate("$key:"),
            StringUtils.translate(TextUtils.trimFancy("  $value", width))
        )
    }

    class ModelWidget(
        structure: Structure,
        scissors: ScissorStack
    ) : ModelWidgetExtension() {
        private val renderable = StructureRenderable.from(structure, scissors)
        private val ready
            get() = renderable.mesh.canRender()
        private var dragging = false

        override fun render(mouseX: Int, mouseY: Int, selected: Boolean, context: DrawContext) {
            if (renderable.mesh.canRender())
                renderable.drawModel(x, y, width)

            RenderUtils.drawOutline(x, y, width, width, Colors.guiBorder.intValue)
        }

        override fun onScrolled(x: Int, y: Int, amount: Double): Boolean {
            if (!ready) return false
            val property = renderable.properties().scale
            val modifier = BrowserConfig.scrollSensitivity.doubleValue * 0.1
            val scale = property.get()
            val linear = ln(scale.toDouble() / 100)
            val newScale = Math.E.pow(linear + amount * modifier) * 100
            property.modify(newScale.toInt() - scale)
            return true
        }

        override fun onClicked(x: Int, y: Int, button: Int): Boolean {
            dragging = true
            return true
        }

        override fun onReleased(x: Int, y: Int, button: Int): Boolean {
            dragging = false
            return true
        }

        override fun onDragged(x: Double, y: Double, dx: Double, dy: Double, button: Int): Boolean {
            if (!ready || !dragging)
                return false
            val modifier = BrowserConfig.scrollSensitivity.doubleValue * 5
            when (button) {
                0 -> {
                    renderable.properties().rotation.modify((dx * modifier).toInt())
                }

                1 -> {
                    renderable.shift(dx, dy)
                }
            }
            return true
        }
    }
}