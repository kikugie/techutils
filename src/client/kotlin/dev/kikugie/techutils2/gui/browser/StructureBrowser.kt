package dev.kikugie.techutils2.gui.browser

import dev.kikugie.techutils2.util.render.ScissorStack
import dev.kikugie.techutils2.gui.icon.IconProvider
import dev.kikugie.techutils2.impl.structure.load.StructureLoader
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import net.minecraft.client.util.math.MatrixStack
import java.io.FileFilter
import java.nio.file.Path
import java.util.Hashtable
import java.util.concurrent.ConcurrentHashMap

class StructureBrowser(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    parent: GuiSchematicBrowserBase,
    listener: ISelectionListener<DirectoryEntry>? = null
) : WidgetSchematicBrowser(x, y, width, height, parent, listener) {
    private val scissorStack = ScissorStack()
    private val supportedFormats = setOf("litematic", "schematic", "schem", "nbt", "bp")
    private val fileFilter = FileFilter { supportedFormats.contains(it.extension) }
    private val metadataCache = mutableMapOf<Path, MetadataWidget?>()
    private var currentMetadataKey: Path? = null
    private var prevMetaStatus = false
    private val showMetadata
        get() = currentMetadataKey != null

    override fun getFileFilter() = fileFilter
    override fun getBrowserWidthForTotalWidth(width: Int) = (width * (if (showMetadata) 0.75F else 1F) - 6).toInt()
    override fun drawAdditionalContents(mouseX: Int, mouseY: Int, context: MatrixStack) {
        if (!showMetadata || currentMetadataKey == null) return
        val currentMetadata = metadataCache[currentMetadataKey] ?: return

        currentMetadata.x = posX + browserWidth + 4
        currentMetadata.y = posY
        currentMetadata.width = totalWidth - browserWidth - 4
        currentMetadata.height = browserHeight

        currentMetadata.render(mouseX, mouseY, true, context)
    }
    override fun onMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        val currentMetadata = currentMetadataKey?.let { metadataCache[it] }
        if (currentMetadata?.onMouseClicked(mouseX, mouseY, mouseButton) == true) return true
        // Clicked on an entry
        val handled = super.onMouseClicked(mouseX, mouseY, mouseButton)
        if (handled) lastSelectedEntry?.let { createMetadataWidget(it.fullPath.toPath()) }
        val relativeY = mouseY - browserEntriesStartY - browserEntriesOffsetY
        val res = relativeY in 0..browserHeight && mouseX - browserEntriesStartX in 0..browserEntryWidth
        if (!handled && res) {
            setLastSelectedEntry(null, -1)
            currentMetadataKey = null
        }
        updateBrowser()
        return res || handled
    }

    override fun createListEntryWidget(
        x: Int,
        y: Int,
        listIndex: Int,
        isOdd: Boolean,
        entry: DirectoryEntry
    ): WidgetDirectoryEntry = DirectoryEntryWidget(
        entry,
        x,
        y,
        browserEntryWidth,
        getBrowserEntryHeightFor(entry),
        listIndex,
        this,
        IconProvider
    )

    @OptIn(DelicateCoroutinesApi::class)
    private fun createMetadataWidget(file: Path) {
        currentMetadataKey = file
        if (metadataCache.containsKey(file)) return
        metadataCache[file] = null
        GlobalScope.launch(Dispatchers.IO) {
            val struct = StructureLoader.load(file)
            synchronized(metadataCache) {
                struct.ifLeft { metadataCache[file] = MetadataWidget(it, scissorStack) }
                struct.ifRight {
                    metadataCache.remove(file)
                    // TODO: Logging
                }
            }
        }
    }

    private fun updateBrowser() {
        if (showMetadata != prevMetaStatus) {
            prevMetaStatus = showMetadata
            setSize(totalWidth, totalHeight)
            refreshEntries()
        }
    }
}