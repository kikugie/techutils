package dev.kikugie.techutils.client.feature.browser

import dev.kikugie.techutils.client.TechUtilsClient
import dev.kikugie.techutils.client.impl.icon.IconProvider
import dev.kikugie.techutils.client.impl.structure.Structure
import dev.kikugie.techutils.client.util.computeIfLoaded
import dev.kikugie.techutils.client.util.multiversion.WidgetSchematicBrowserExtension
import dev.kikugie.techutils.mixin.mod.litematica.widget.WidgetFileBrowserBaseAccessor
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry
import fi.dy.masa.malilib.util.StringUtils
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.minecraft.client.gui.DrawContext
import java.io.File
import java.io.FileFilter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

    private val metadataCache = ConcurrentHashMap<File, Optional<MetadataWidget>>()
    private var currentMetadata: MetadataWidget? = null
    private val showMetadata
        get() = currentMetadata != null
    private var prevMetaStatus = false
    private var loadTask: Pair<DirectoryEntry, Deferred<Structure>>? = null

    init {
        computeIfLoaded("axiom") { supportedFormats.add("bp") }
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

    override fun onReleased(x: Int, y: Int, button: Int): Boolean {
        currentMetadata?.onMouseReleased(x, y, button)
        return true
    }

    override fun onScrolled(x: Int, y: Int, amount: Double) =
        currentMetadata?.onScrolled(x, y, amount) ?: false


    override fun onDragged(x: Double, y: Double, dx: Double, dy: Double, button: Int) =
        currentMetadata?.onDragged(x, y, dx, dy, button) ?: false

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createMetadataWidget(entry: DirectoryEntry) {
        val file = entry.fullPath
        if (loadTask?.first != entry)
            loadTask?.second?.let {
                it.cancel()
                metadataCache.remove(file)
            }

        if (!metadataCache.containsKey(file)) {
            metadataCache[file] = Optional.empty()
            val job = Structure.loadAsync(entry)
            loadTask = entry to job
            job.invokeOnCompletion { error ->
                if (error != null)
                    TechUtilsClient.LOGGER.warn(error.message, error)
                else
                    MetadataWidget(job.getCompleted()).also {
                        metadataCache[file] = Optional.of(it)
                        currentMetadata = it
                        updateBrowser()
                    }
            }
        }
        currentMetadata = metadataCache[file]?.orElse(null)
    }

    private fun updateBrowser() {
        if (showMetadata != prevMetaStatus) {
            prevMetaStatus = showMetadata
            setSize(totalWidth, totalHeight)
            refreshEntries()
        }
    }

    override fun createListEntryWidget(
        x: Int,
        y: Int,
        listIndex: Int,
        isOdd: Boolean,
        entry: DirectoryEntry
    ): WidgetDirectoryEntry {
        return DirectoryEntryWidget(
            entry,
            x,
            y,
            browserEntryWidth,
            getBrowserEntryHeightFor(entry),
            listIndex,
            this,
            IconProvider
        )
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