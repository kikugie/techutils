package dev.kikugie.techutils.client.feature.downloader

import dev.kikugie.techutils.client.feature.serializer.PlacementSerializer
import dev.kikugie.techutils.client.util.litematica.InGameNotifier
import dev.kikugie.techutils.client.util.multiversion.floor
import dev.kikugie.techutils.client.util.render.Colors
import dev.kikugie.techutils.client.util.render.TextUtils
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
import kotlinx.coroutines.runBlocking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.math.BlockPos
import java.io.File
import kotlin.math.max

class DownloaderGui : GuiDialogBase() {
    private val originalLink = getClipboardText()
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
        var placement: SchematicPlacement? = null
        if (runBlocking {
                // Indent *dies*
                try {
                    SchematicDownloader.download(urlField.text, force) { placement = createPlacement(it) }
                    false
                } catch (e: Exception) {
                    addMessage(Message.MessageType.ERROR, e.message ?: "techutils.download.downloadFail")
                    true
                }
            }) return

        openGui(parent)
        if (placementData.text == "" || placement == null) return
        if (!PlacementSerializer.deserialize(placementData.text, placement!!))
            InGameNotifier.addMessage(Message.MessageType.ERROR, "techutils.downloader.dataFail")
    }

    private fun createPlacement(file: File): SchematicPlacement? {
        // TODO: Use Structure.load() instead
        val schematic = LitematicaSchematic.createFromFile(file.parentFile, file.name)
        if (schematic == null) {
            addMessage(Message.MessageType.ERROR, "techutils.downloader.failLoad")
            return null
        }
        SchematicHolder.getInstance().addSchematic(schematic, true)
        return if (DataManager.getCreatePlacementOnLoad()) {
            val pos = MinecraftClient.getInstance().player?.pos?.floor() ?: BlockPos.ORIGIN
            val name = schematic.metadata.name
            val manager = DataManager.getSchematicPlacementManager()
            val placement = SchematicPlacement.createFor(schematic, pos, name, true, true)
            manager.addSchematicPlacement(placement, true)
            manager.selectedSchematicPlacement = placement
            placement
        } else null
    }

    public override fun drawContents(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        parent?.render(context, mouseX, mouseY, partialTicks)

        val matrixStack = context.matrices
        matrixStack.push()
        matrixStack.translate(0f, 0f, .5f)
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
        private fun getClipboardText(): String {
            val clipboard = TextUtils.clipboard
            return if (clipboard != null && SchematicDownloader.verifyLink(clipboard) != null) clipboard else ""
        }
    }
}