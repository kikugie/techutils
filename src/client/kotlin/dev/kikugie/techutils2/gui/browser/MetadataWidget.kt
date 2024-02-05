package dev.kikugie.techutils2.gui.browser

import dev.kikugie.techutils2.gui.util.Colors
import dev.kikugie.techutils2.gui.util.Draggable
import dev.kikugie.techutils2.impl.structure.Structure
import dev.kikugie.techutils2.util.TextUtils
import dev.kikugie.techutils2.util.render.ScissorStack
import fi.dy.masa.malilib.gui.widgets.WidgetContainer
import fi.dy.masa.malilib.render.RenderUtils
import fi.dy.masa.malilib.util.StringUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.sign

@OptIn(DelicateCoroutinesApi::class)
class MetadataWidget(
    private val structure: Structure,
    private val scissorStack: ScissorStack,
) : WidgetContainer(0, 0, 0, 0), Draggable {
    private val translations: List<String> = structure.metadata.translations.map { (_, v) -> v() }
    private var lines = refreshLines()
    private var model: ModelWidget? = null
    private var scroll: Int = 0

    init {
        GlobalScope.launch(Dispatchers.Default) {
            model = ModelWidget(structure, scissorStack)
        }
    }

    override fun render(mouseX: Int, mouseY: Int, selected: Boolean, context: MatrixStack) {
        RenderUtils.drawOutlinedBox(x, y, width, height, Colors.guiBackground.intValue, Colors.guiBorder.intValue)
//        scissorStack.push(x + 2, y + 2, x + width - 4, y + height - 4)
        drawMetadataWidget(mouseX, mouseY, selected, context)
//        scissorStack.pop()
    }

    private fun drawMetadataWidget(mouseX: Int, mouseY: Int, selected: Boolean, context: MatrixStack) {
        val lx = x + 4
        val ly = y + 4
        val lw = width - 8
        val lh = height - 4
        for (i in scroll until lines.size) {
            val line = lines[i]
            val textY = ly + (i - scroll) * 10
            StringUtils.drawString(lx, textY, Colors.guiText.intValue, line, context)
        }

        model?.apply {
            x = lx
            y = ly + (lines.size - scroll) * 10
            width = lw
            height = lh
            render(mouseX, mouseY, selected, context)
        }
    }

    private fun refreshLines() = translations.map { TextUtils.trimFancy(it, width - 8) }

    override fun onMouseClickedImpl(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return model?.onMouseClicked(mouseX, mouseY, mouseButton) == true
    }

    override fun onMouseReleasedImpl(mouseX: Int, mouseY: Int, mouseButton: Int) {
        model?.onMouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun onMouseScrolledImpl(mouseX: Int, mouseY: Int, mouseWheelDelta: Double): Boolean {
        if (model?.onMouseScrolled(mouseX, mouseY, mouseWheelDelta) == true) return true
        scroll += sign(-mouseWheelDelta).toInt()
        scroll = scroll.coerceAtMost(translations.size - 1)
        return true
    }

    override fun onDragged(x: Double, y: Double, dx: Double, dy: Double, button: Int): Boolean {
        return model?.onDragged(x, y, dx, dy, button) == true
    }

    override fun setWidth(width: Int) {
        super.setWidth(width)
        lines = refreshLines()
    }
}