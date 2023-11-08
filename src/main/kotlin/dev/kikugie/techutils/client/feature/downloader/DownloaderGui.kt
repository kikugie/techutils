package dev.kikugie.techutils.client.feature.downloader

import fi.dy.masa.litematica.gui.Icons
import fi.dy.masa.malilib.gui.GuiTextInputBase
import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.gui.widgets.WidgetCheckBox
import fi.dy.masa.malilib.util.GuiUtils
import fi.dy.masa.malilib.util.StringUtils
import kotlinx.coroutines.runBlocking
import net.minecraft.client.gui.DrawContext
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File

class DownloaderGui(private val action: (File) -> Unit) : GuiTextInputBase(
    512,
    "techutils.downloader.title",
    getClipboardText(),
    GuiUtils.getCurrentScreen()
) {
    private var force = false
    private val checkbox = WidgetCheckBox(
        textField.x,
        textField.y - 16,
        Icons.CHECKBOX_UNSELECTED,
        Icons.CHECKBOX_SELECTED,
        StringUtils.translate("techutils.downloader.force")
    )

    init {
        checkbox.setListener { force = it?.isChecked ?: false }
        addWidget(checkbox)
    }

    override fun drawContents(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawContents(context, mouseX, mouseY, partialTicks)
        checkbox.render(mouseX, mouseY, false, context)
    }

    companion object {
        private fun getClipboardText(): String {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val transferable = clipboard.getContents(null)
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                val value = transferable.getTransferData(DataFlavor.stringFlavor) as String
                if (SchematicDownloader.verifyLink(value) != null) return value
            }
            return ""
        }
    }

    override fun applyValue(string: String): Boolean {
        var out: Boolean
        runBlocking {
            out = try {
                SchematicDownloader.download(string, force, action)
                true
            } catch (e: Exception) {
                addMessage(Message.MessageType.ERROR, e.message ?: "techutils.download.downloadFail")
                false
            }
        }
        return out
    }
}