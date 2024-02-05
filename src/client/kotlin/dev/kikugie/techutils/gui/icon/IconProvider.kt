package dev.kikugie.techutils.gui.icon

import fi.dy.masa.litematica.gui.Icons
import fi.dy.masa.malilib.gui.interfaces.IFileBrowserIconProvider
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntryType
import java.io.File

object IconProvider : IFileBrowserIconProvider {
    override fun getIconForFile(file: File) = when {
        file.exists() -> GuiIcon.ERROR
        file.isDirectory -> Icons.FILE_ICON_DIR
        else -> getIconForExtension(file.extension)
    }

    fun get(entry: WidgetFileBrowserBase.DirectoryEntry?) = when (entry?.type) {
        DirectoryEntryType.FILE -> getIconForExtension(entry.fullPath.extension)
        DirectoryEntryType.DIRECTORY -> Icons.FILE_ICON_DIR
        DirectoryEntryType.INVALID -> GuiIcon.ERROR
        else -> GuiIcon.UNKNOWN
    }

    private fun getIconForExtension(extension: String) = when (extension) {
        "litematic" -> Icons.FILE_ICON_LITEMATIC
        "schematic" -> Icons.FILE_ICON_SCHEMATIC
        "schem" -> Icons.FILE_ICON_SPONGE_SCH
        "nbt" -> Icons.FILE_ICON_VANILLA
        "json" -> Icons.FILE_ICON_JSON
        "bp" -> GuiIcon.BLUEPRINT
        else -> GuiIcon.UNKNOWN
    }

    override fun getIconRoot() = Icons.FILE_ICON_DIR_ROOT
    override fun getIconUp() = Icons.FILE_ICON_DIR_UP
    override fun getIconCreateDirectory() = Icons.FILE_ICON_CREATE_DIR
    override fun getIconSearch() = Icons.FILE_ICON_SEARCH
    override fun getIconDirectory() = Icons.FILE_ICON_DIR
}