package dev.kikugie.techutils.client.feature.browser

import dev.kikugie.techutils.client.feature.browser.preview.StructureRenderable
import dev.kikugie.techutils.client.impl.structure.Structure
import dev.kikugie.techutils.client.util.computeIfLoaded
import dev.kikugie.techutils.client.util.multiversion.MetadataWidgetExtension
import dev.kikugie.techutils.client.util.multiversion.ModelWidgetExtension
import dev.kikugie.techutils.client.util.render.Colors
import dev.kikugie.techutils.client.util.render.ScissorStack
import dev.kikugie.techutils.client.util.render.TextUtils
import fi.dy.masa.malilib.render.RenderUtils
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.math.MathHelper
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sign

class MetadataWidget(
    private val structure: Structure
    // Dimensions are updated at render time
) : MetadataWidgetExtension() {
    private val scissors = ScissorStack()
    private val model = computeIfLoaded("isometric-renders") { ModelWidget(structure, scissors) }
    private var scroll = 0
    private val keyTranslations = structure.metadata.map.keys.associateWith { StringUtils.translate(it) }

    init {
        if (model != null) addWidget(model)
    }

    override fun onScrolled(x: Int, y: Int, amount: Double): Boolean {
        if (!isMouseOver(x, y)) return false
        if (model?.onScrolled(x, y, amount) == true) return true
        scroll = MathHelper.clamp(scroll + sign(-amount).toInt(), 0, structure.metadata.map.size - 1)
        return true
    }

    override fun onDragged(x: Double, y: Double, dx: Double, dy: Double, button: Int) =
        model?.onDragged(x, y, dx, dy, button) ?: false

    override fun render(mouseX: Int, mouseY: Int, selected: Boolean, context: DrawContext) {
        RenderUtils.drawOutlinedBox(x, y, width, height, Colors.guiBackground.intValue, Colors.guiBorder.intValue)
        scissors.pushDirect(x + 2, y + 2, width - 4, height - 4)
        drawMetadataWidget(mouseX, mouseY, selected, context)
        scissors.pop()
    }

    private fun drawMetadataWidget(mouseX: Int, mouseY: Int, selected: Boolean, context: DrawContext) {
        val lx = x + 4
        val ly = y + 4
        val lw = width - 8
        val lh = height - 4

        val lines = arrayListOf<String>()
        structure.metadata.map.forEach { (k, v) ->
            lines.addAll(formatLine(keyTranslations[k] ?: k, v, lw))
        }
        for (i in scroll until lines.size) {
            val line = lines[i]
            val textY = ly + (i - scroll) * 10
            StringUtils.drawString(lx, textY, Colors.guiText.intValue, line, context)
        }

        if (model != null) {
            model.x = lx
            model.y = ly + (lines.size - scroll) * 10
            model.width = lw
            model.height = lh
            model.render(mouseX, mouseY, selected, context)
        }
    }

    private fun formatLine(key: String, value: String, width: Int): List<String> {
        val combined = "$key: $value"
        return if (TextUtils.isWithin(combined, width))
            listOf(StringUtils.translate(combined))
        else listOf(
            TextUtils.trimFancy("$key: ", width),
            TextUtils.trimFancy("  $value", width)
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

            renderable.scissors.run(ScissorStack.ScreenRect.ofDims(x, y, width, width)) {
                RenderUtils.drawOutline(x, y, width, width, Colors.guiBorder.intValue)
            }
        }

        override fun onScrolled(x: Int, y: Int, amount: Double): Boolean {
            if (!ready || !isMouseOver(x, y)) return false
            val property = renderable.properties().scale
            val modifier = BrowserConfig.scrollSensitivity.doubleValue * 0.1
            val scale = property.get()
            val linear = ln(scale.toDouble() / 100)
            val newScale = Math.E.pow(linear + amount * modifier) * 100
            val delta = ceil(newScale - scale).toInt()
            property.modify(delta)
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
            if (!isMouseOver(x.toInt(), y.toInt())) {
                dragging = false
                return true
            }
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