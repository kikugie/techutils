package dev.kikugie.techutils.client.feature.preview.render

import dev.kikugie.techutils.client.feature.preview.PreviewConfig
import dev.kikugie.techutils.client.util.render.ScissorStack
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import fi.dy.masa.malilib.render.RenderUtils
import fi.dy.masa.malilib.util.Color4f
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.pow

open class PreviewManager {
    private val bgColor = Color4f(0F, 0F, 0F, 0.627451F)
    private val borderColor = Color4f(0.6F, 0.6F, 0.6F, 1F)
    private val loadingText = Text.of("Loading...")
    private val cache: MutableMap<DirectoryEntry, StructureRenderable?> = mutableMapOf()
    private var active: StructureRenderable? = null

    private var x = 0
    private var y = 0
    private var size = 0
    private var drag = false

    fun drawPreview(entry: DirectoryEntry?, scissors: ScissorStack, x: Int, y: Int, size: Int) {
        if (entry == null) return
        active = cache[entry]

        this.x = x
        this.y = y
        this.size = size

        RenderUtils.drawOutlinedBox(x, y, size, size, bgColor.intValue, borderColor.intValue)
    }

    fun onScroll(x: Double, y: Double, amount: Double) {
        if (!inViewport(x, y))
            return
        val property = active!!.properties().scale
        val modifier = PreviewConfig.scaleSensitivity.doubleValue * 0.1
        val scale = property.get()
        val linear = ln(scale.toDouble() / 100)
        val newScale = Math.E.pow(linear + amount * modifier) * 100
        property.modify(newScale.toInt() - scale)
    }

    fun onClick(x: Double, y: Double, button: Int) {
        if (inViewport(x, y))
            drag = true
    }

    fun onRelease(x: Double, y: Double, button: Int) {
        drag = false
    }

    fun onDrag(x: Double, y: Double, dx: Double, dy: Double, button: Int) {
        if (!inViewport(x, y))
            drag = false
        if (!drag)
            return

        val modifier = PreviewConfig.rotationSensitivity.doubleValue * 10
        when (button) {
            0 -> {
                active!!.properties().rotation.modify((dx * modifier).toInt())
            }

            1 -> {
                active!!.shift(dx, dy)
            }
        }
    }

    private fun inViewport(x: Double, y: Double): Boolean {
        return active != null && x > this.x && y > this.y && x < this.x + this.size && y < this.y + this.size
    }

    private fun drawLoading(context: DrawContext, x: Int, y: Int, size: Int) {
        val textRenderer = MinecraftClient.getInstance().textRenderer
        val textWidth = min(textRenderer.getWidth(loadingText), size - 4)
        context.drawTextWrapped(
            textRenderer,
            loadingText,
            x + size / 2 - textWidth / 2,
            y + size / 2 - 4,
            textWidth,
            borderColor.intValue
        )
    }

    interface Accessor {
        val previewManager: PreviewManager
    }
}