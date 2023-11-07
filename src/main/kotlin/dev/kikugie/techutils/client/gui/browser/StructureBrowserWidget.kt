package dev.kikugie.techutils.client.gui.browser

import dev.kikugie.techutils.client.TechUtilsClient
import dev.kikugie.techutils.client.gui.icon.IconProvider
import dev.kikugie.techutils.client.impl.structure.Structure
import dev.kikugie.techutils.client.util.FabricUtils
import dev.kikugie.techutils.client.util.multiversion.WidgetSchematicBrowserExtension
import dev.kikugie.techutils.mixin.mod.litematica.widget.WidgetFileBrowserBaseAccessor
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase
import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener
import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.client.gui.DrawContext
import java.io.File
import java.io.FileFilter

class StructureBrowserWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    parent: GuiSchematicBrowserBase,
    selectionListener: ISelectionListener<DirectoryEntry>? = null
) : WidgetSchematicBrowserExtension(x, y, width, height, parent, selectionListener) {
    private val supportedFormats = mutableListOf("litematic", "schematic", "schem", "nbt")
    private val fileFilter = FileFilter { file: File ->
        supportedFormats.contains(file.extension)
    }
    /*
    private val scissors = ScissorStack()
    private val worldEditSchematics = DirectoryEntry(
        DirectoryEntryType.DIRECTORY,
        FabricLoader.getInstance().configDir.resolve("worldedit/schematics").toFile(),
        "WorldEdit Schematics",
        null
    )
    private val axiomBlueprints = DirectoryEntry(
        DirectoryEntryType.DIRECTORY,
        FabricLoader.getInstance().configDir.resolve("axiom/blueprints").toFile(),
        "Axiom Blueprints",
        null
    )
    */

    private val metadataCache = mutableMapOf<File, MetadataWidget?>()
    private var currentMetadata: MetadataWidget? = null
    private val showMetadata
        get() = currentMetadata != null
    private var prevMetaStatus = false

    init {
        if (FabricUtils.isLoaded("axiom"))
            supportedFormats.add("bp")
        title = "${StringUtils.translate("litematica.gui.title.schematic_browser", *arrayOfNulls(0))} (TechUtils)"
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as WidgetFileBrowserBaseAccessor).iconProvider = IconProvider
    }

    override fun getRootDirectory(): File {
        return DataManager.getSchematicsBaseDirectory()
    }

    override fun getFileFilter(): FileFilter {
        return fileFilter
    }

    override fun getBrowserWidthForTotalWidth(width: Int): Int {
        return (width * (if (showMetadata) 0.75F else 1F) - 6).toInt()
    }

    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        currentMetadata?.onMouseClicked(mouseX, mouseY, mouseButton)
        // Clicked on an entry
        val handled = super.onMouseClicked(mouseX, mouseY, mouseButton)
        if (handled) lastSelectedEntry?.let { createMetadataWidget(it) }
        val relativeY = mouseY - browserEntriesStartY - browserEntriesOffsetY
        val res = relativeY in 0..browserHeight && mouseX - browserEntriesStartX in 0..browserEntryWidth
        if (!handled && res) {
            setLastSelectedEntry(null, -1)
            currentMetadata = null
        }
        updateBrowser()
        return res || handled
    }

    override fun onMouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        currentMetadata?.onMouseReleased(mouseX, mouseY, mouseButton)
        return super.onMouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun onScrolled(x: Int, y: Int, amount: Double) =
        currentMetadata?.onScrolled(x, y, amount) ?: false

    override fun onReleased(x: Int, y: Int, button: Int): Boolean {
        currentMetadata?.onMouseReleased(x, y, button)
        return true
    }

    override fun onDragged(x: Double, y: Double, dx: Double, dy: Double, button: Int) =
        currentMetadata?.onDragged(x, y, dx, dy, button) ?: false

    private fun createMetadataWidget(entry: DirectoryEntry) {
        val file = entry.fullPath
        if (!metadataCache.containsKey(file))
            metadataCache[file] = try {
                Structure.load(entry)
            } catch (e: Exception) {
                TechUtilsClient.LOGGER.warn(e.message, e)
                parent.addMessage(Message.MessageType.ERROR, e.message)
                null
            }?.let { MetadataWidget(it) }
        currentMetadata = metadataCache[file]
    }

    private fun updateBrowser() {
        if (showMetadata != prevMetaStatus) {
            prevMetaStatus = showMetadata
            setSize(totalWidth, totalHeight)
            refreshEntries()
        }
    }

    override fun reCreateListEntryWidgets() {
        listWidgets.clear()
        maxVisibleBrowserEntries = 0

        val listHeight = browserHeight - browserPaddingY - browserEntriesOffsetY
        val x = posX + 2
        val y = posY + 4 + browserEntriesOffsetY
        var yOffset = 0

        val header = createHeaderWidget(x, y, scrollBar.value, browserWidth, 0)
        if (header != null) {
            listWidgets.add(header)
            yOffset = header.height
        }

        for (i in scrollBar.value + 1 until listContents.size) {
            val entry = listContents[i]
            val height = getBrowserEntryHeightFor(entry)
            if (yOffset + height > listHeight) break
            listWidgets.add(
                DirectoryEntryWidget(
                    entry,
                    x,
                    y + yOffset,
                    browserEntryWidth,
                    height,
                    i,
                    this,
                    IconProvider
                )
            )
            maxVisibleBrowserEntries++
            yOffset += height
        }

        scrollBar.maxValue = listContents.size - maxVisibleBrowserEntries
    }

    override fun drawAdditionalContents(mouseX: Int, mouseY: Int, context: DrawContext) {
        if (!showMetadata || currentMetadata == null) return

        currentMetadata!!.x = posX + browserWidth + 4
        currentMetadata!!.y = posY
        currentMetadata!!.width = totalWidth - browserWidth - 4
        currentMetadata!!.height = browserHeight

        currentMetadata!!.render(mouseX, mouseY, false, context)
    }
}