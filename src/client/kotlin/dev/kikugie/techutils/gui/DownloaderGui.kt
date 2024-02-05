package dev.kikugie.techutils.gui

import dev.kikugie.techutils.util.InGameNotifier
import dev.kikugie.techutils.gui.util.Colors
import dev.kikugie.techutils.impl.DownloadResult
import dev.kikugie.techutils.impl.SchematicDownloader
import dev.kikugie.techutils.impl.serializer.PlacementSerializer
import dev.kikugie.techutils.util.TextUtils
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.data.SchematicHolder
import fi.dy.masa.litematica.gui.Icons
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.malilib.gui.GuiDialogBase
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric
import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.gui.widgets.WidgetCheckBox
import fi.dy.masa.malilib.render.RenderUtils
import fi.dy.masa.malilib.util.GuiUtils
import fi.dy.masa.malilib.util.KeyCodes
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.math.max

/**
 * // dev info
 * ## Translations
 * - `techutils.downloader.link()`
 * - `techutils.downloader.data()`
 * - `techutils.downloader.title()`
 * - `techutils.downloader.force()`
 * - `techutils.downloader.invalid_link()`
 * - `techutils.downloader.download_fail(error)`
 * - `techutils.downloader.load_fail()`
 * - `techutils.downloader.data_fail()`
 */
class DownloaderGui : GuiDialogBase() {
    private val originalLink = getClipboardLink()
    private val linkText = StringUtils.translate("techutils.downloader.link")
    private val dataText = StringUtils.translate("techutils.downloader.data")

    private val urlField: GuiTextFieldGeneric
    private val forceDownload: WidgetCheckBox
    private val placementData: GuiTextFieldGeneric

    private var force = false

    init {
        setParent(GuiUtils.getCurrentScreen())
        title = StringUtils.translate("techutils.downloader.title")
        useTitleHierarchy = false

        setWidthAndHeight(360, 150)
        centerOnScreen()

        urlField = GuiTextFieldGeneric(
            dialogLeft + 12,
            dialogTop + 30,
            dialogWidth - 20,
            20,
            textRenderer
        )
        urlField.isFocused = true
        urlField.text = originalLink

        forceDownload = WidgetCheckBox(
            urlField.x,
            urlField.y + urlField.height + 2,
            Icons.CHECKBOX_UNSELECTED,
            Icons.CHECKBOX_SELECTED,
            StringUtils.translate("techutils.downloader.force")
        )
        forceDownload.setListener { force = it?.isChecked ?: false }
        addWidget(forceDownload)

        placementData = GuiTextFieldGeneric(
            urlField.x,
            urlField.y + urlField.height + 30,
            urlField.width,
            urlField.height,
            textRenderer
        )
    }

    override fun initGui() {
        var x = dialogLeft + 12
        val y = dialogTop + dialogHeight - 30

        x += createButton(x, y, "malilib.gui.button.ok") { apply() } + 2
        x += createButton(x, y, "malilib.gui.button.cancel") { openGui(parent) } + 2
        createButton(x, y, "malilib.gui.button.reset") {
            urlField.text = ""
            placementData.text = ""
        }
    }

    private fun apply() {
        if (!SchematicDownloader.verifyLink(urlField.text)) {
            addMessage(Message.MessageType.ERROR, "techutils.download.invalid_link")
        } else {
            SchematicDownloader.download(urlField.text, force, ::processResult)
            closeGui(true)
        }
    }

    private fun processResult(result: DownloadResult) {
        result.ifRight { addMessage(Message.MessageType.ERROR, "techutils.download.download_fail", it.message) }
        result.ifLeft(::createPlacement)
    }

    private fun createPlacement(file: Path) {
        // TODO: Use Structure.load() instead
        val schematic = LitematicaSchematic.createFromFile(file.parent.toFile(), file.fileName.name)
        if (schematic == null) {
            addMessage(Message.MessageType.ERROR, "techutils.downloader.load_fail")
            return
        }
        SchematicHolder.getInstance().addSchematic(schematic, true)
        if (!DataManager.getCreatePlacementOnLoad()) return

        val pos = BlockPos.ofFloored(MinecraftClient.getInstance().player?.pos ?: Vec3d.ZERO)
        val name = schematic.metadata.name
        val manager = DataManager.getSchematicPlacementManager()
        val placement = SchematicPlacement.createFor(schematic, pos, name, true, true)
        manager.addSchematicPlacement(placement, true)
        manager.selectedSchematicPlacement = placement

        if (placementData.text != "" && !PlacementSerializer.deserialize(placementData.text, placement!!))
            InGameNotifier.addMessage(Message.MessageType.ERROR, "techutils.downloader.data_fail")
    }

    public override fun drawContents(context: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        parent?.render(context, mouseX, mouseY, partialTicks)

        val matrixStack = context
        matrixStack.push()
        matrixStack.translate(0.0, 0.0, 1.0)
        RenderUtils.drawOutlinedBox(
            dialogLeft,
            dialogTop, dialogWidth, dialogHeight, -0x20000000, COLOR_HORIZONTAL_BAR
        )

        drawStringWithShadow(context, titleString, dialogLeft + 10, dialogTop + 4, Colors.guiText.intValue)

        val fontHeight = textRenderer.fontHeight
        drawStringWithShadow(context, linkText, urlField.x, urlField.y - fontHeight - 2, Colors.guiText.intValue)
        urlField.render(context, mouseX, mouseY, partialTicks)
        forceDownload.render(mouseX, mouseY, false, context)

        drawStringWithShadow(
            context,
            dataText,
            placementData.x,
            placementData.y - fontHeight - 2,
            Colors.guiText.intValue
        )
        placementData.render(context, mouseX, mouseY, partialTicks)

        drawButtons(mouseX, mouseY, partialTicks, context)
        matrixStack.pop()
    }

    override fun onKeyTyped(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == KeyCodes.KEY_ESCAPE) {
            openGui(parent)
            return true
        }
        return if (urlField.isFocused) {
            urlField.keyPressed(keyCode, scanCode, modifiers)
        } else if (placementData.isFocused) {
            placementData.keyPressed(keyCode, scanCode, modifiers)
        } else super.onKeyTyped(keyCode, scanCode, modifiers)
    }

    override fun onCharTyped(charIn: Char, modifiers: Int): Boolean {
        return if (urlField.isFocused) {
            urlField.charTyped(charIn, modifiers)
        } else if (placementData.isFocused) {
            placementData.charTyped(charIn, modifiers)
        } else super.onCharTyped(charIn, modifiers)
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, button: Int): Boolean {
        return if (urlField.mouseClicked(mouseX.toDouble(), mouseY.toDouble(), button)) {
            placementData.isFocused = false
            true
        } else if (placementData.mouseClicked(mouseX.toDouble(), mouseY.toDouble(), button)) {
            urlField.isFocused = false
            true
        } else super.onMouseClicked(mouseX, mouseY, button)
    }

    private fun createButton(x: Int, y: Int, key: String, action: (GuiDialogBase) -> Unit): Int {
        val button = ButtonGeneric(x, y, -1, 20, StringUtils.translate(key))
        button.width = max(40, button.width)
        return addButton(button) { _, mouseButton -> if (mouseButton == 0) action(this@DownloaderGui) }.width
    }

    companion object {
        private fun getClipboardLink(): String = TextUtils.clipboard?.takeIf { SchematicDownloader.verifyLink(it) } ?: ""
    }
}