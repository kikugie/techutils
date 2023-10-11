package dev.kikugie.techutils.client.feature.preview.render

import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import net.minecraft.client.gui.DrawContext

interface PreviewInvoker {
    fun drawPreview(entry: DirectoryEntry?, context: DrawContext, x: Int, y: Int, size: Int)
}