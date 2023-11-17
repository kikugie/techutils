package dev.kikugie.techutils.client.feature.browser

import dev.kikugie.techutils.client.impl.icon.IconProvider
import dev.kikugie.techutils.client.util.render.Colors
import fi.dy.masa.malilib.gui.interfaces.IDirectoryNavigator
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntryType
import fi.dy.masa.malilib.render.RenderUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
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
    private val textRenderer = MinecraftClient.getInstance().textRenderer
    private val filename = File(entry.name).nameWithoutExtension
    private val textWidth = textRenderer.getWidth(filename)
    private val dotWidth = textRenderer.getWidth("...")
    private var cachedText: String? = null

    override fun render(mouseX: Int, mouseY: Int, selected: Boolean, context: DrawContext) {
        val color = if (selected || isMouseOver(mouseX, mouseY))
            Colors.listEntryHovered
        else if (isOdd)
            Colors.listEntryOdd
        else
            Colors.listEntryEven
        RenderUtils.drawRect(x, y, width, height, color.intValue)
        if (selected)
            RenderUtils.drawOutline(x, y, width, height, Colors.guiBorder.intValue)

        // 1 | icon | 2 | text | 1
        val availableWidth = width - icon.width - 4
        val text: String = if (textWidth > availableWidth)
            getTextForWidth(availableWidth)
        else
            filename

        val yOffset = (height - fontHeight) / 2 + 1

        bindTexture(icon.texture)
        icon.renderAt(x + 1, y + 1, zLevel + 10F, false, selected)
        drawString(x + icon.width + 3, y + yOffset, Colors.guiText.intValue, text, context)
        drawSubWidgets(mouseX, mouseY, context)
    }

    private fun getTextForWidth(width: Int): String {
        return if (cachedText != null) cachedText!!
        else {
            val text = textRenderer.trimToWidth(filename, width - dotWidth) + "..."
            cachedText = text
            text
        }
    }

    override fun setWidth(width: Int) {
        super.setWidth(width)
        cachedText = null
    }

    override fun onMouseClickedImpl(mouseX: Int, mouseY: Int, button: Int): Boolean {
        return when (button) {
            0 -> {
                if (entry!!.type == DirectoryEntryType.DIRECTORY) {
                    navigator.switchToDirectory(entry!!.fullPath)
                }
                true
            }

            else -> false
        }
    }
}