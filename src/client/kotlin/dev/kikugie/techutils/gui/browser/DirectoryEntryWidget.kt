package dev.kikugie.techutils.gui.browser

import dev.kikugie.techutils.gui.util.Colors
import dev.kikugie.techutils.gui.icon.IconProvider
import fi.dy.masa.malilib.gui.interfaces.IDirectoryNavigator
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import fi.dy.masa.malilib.render.RenderUtils
import net.minecraft.client.util.math.MatrixStack
import java.io.File

class DirectoryEntryWidget(
    entry: DirectoryEntry,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    index: Int,
    navigator: IDirectoryNavigator,
    iconProvider: IconProvider
) : WidgetDirectoryEntry(x, y, width, height, index % 2 == 1, entry, index, navigator, iconProvider) {
    /* Metadata */
    private val icon = iconProvider.get(entry)
    private val filename = File(entry.name).nameWithoutExtension
    private val textWidth = textRenderer.getWidth(filename)
    private val dotWidth = textRenderer.getWidth("...")
    private var cachedText: String? = null

    override fun render(mouseX: Int, mouseY: Int, selected: Boolean, context: MatrixStack) {
        val color = when {
            selected || isMouseOver(mouseX, mouseY) -> Colors.listEntryHovered
            isOdd -> Colors.listEntryOdd
            else -> Colors.listEntryEven
        }
        RenderUtils.drawRect(x, y, width, height, color.intValue)
        if (selected)
            RenderUtils.drawOutline(x, y, width, height, Colors.guiBorder.intValue)

        // 1 | icon | 2 | text | 1
        val availableWidth = width - icon.width - 4
        val text: String = if (textWidth > availableWidth)
            getTextForWidth(availableWidth)
        else filename

        val yOffset = (height - fontHeight) / 2 + 1

        bindTexture(icon.texture)
        icon.renderAt(x + 1, y + 1, zLevel + 10F, false, selected)
        drawString(x + icon.width + 3, y + yOffset, Colors.guiText.intValue, text, context)
        drawSubWidgets(mouseX, mouseY, context)
    }

    private fun getTextForWidth(width: Int) =
        if (cachedText != null) cachedText!!
        else textRenderer.trimToWidth(filename, width - dotWidth) + "...".also { cachedText = it }

    override fun setWidth(width: Int) {
        super.setWidth(width)
        cachedText = null
    }

    override fun onMouseClickedImpl(mouseX: Int, mouseY: Int, button: Int): Boolean {
        return when (button) {
            0 -> {
                if (entry!!.type == WidgetFileBrowserBase.DirectoryEntryType.DIRECTORY) {
                    navigator.switchToDirectory(entry!!.fullPath)
                }
                true
            }

            else -> false
        }
    }
}