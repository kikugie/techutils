package dev.kikugie.techutils2.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import java.awt.HeadlessException
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

object TextUtils {
    val textRenderer: TextRenderer
        get() = MinecraftClient.getInstance().textRenderer

    var clipboard: String?
        get() = try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val transferable = clipboard.getContents(null)
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                transferable.getTransferData(DataFlavor.stringFlavor) as String
            } else null
        } catch (ignored: HeadlessException) {
            null
        }
        set(value) {
            val stringSelection = StringSelection(value)
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(stringSelection, null)
        }

    fun trimFancy(text: String, width: Int): String {
        if (isWithin(text, width)) return text

        val dotWidth = textRenderer.getWidth("...")
        if (dotWidth > width) return ""

        return "${textRenderer.trimToWidth(text, width - dotWidth)}..."
    }

    fun isWithin(text: String, width: Int): Boolean {
        return textRenderer.getWidth(text) <= width
    }
}
