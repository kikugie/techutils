package dev.kikugie.techutils.client.feature.browser.icon

import fi.dy.masa.litematica.gui.Icons
import fi.dy.masa.malilib.gui.interfaces.IFileBrowserIconProvider
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntryType
import java.io.File

object IconProvider : IFileBrowserIconProvider {
    override fun getIconForFile(file: File): IGuiIcon {
        if (!file.exists())
            return GuiIcon.errorFile
        if (file.isDirectory)
            return Icons.FILE_ICON_DIR
        return getIconForExtension(file.extension)
    }

    fun get(entry: WidgetFileBrowserBase.DirectoryEntry?): IGuiIcon {
        return when (entry?.type) {
            DirectoryEntryType.FILE -> getIconForExtension(entry.fullPath.extension)
            DirectoryEntryType.DIRECTORY -> Icons.FILE_ICON_DIR
            DirectoryEntryType.INVALID -> GuiIcon.errorFile
            else -> GuiIcon.unknownFile
        }
    }

    private fun getIconForExtension(extension: String): IGuiIcon {
        return when (extension) {
            "litematic" -> Icons.FILE_ICON_LITEMATIC
            "schematic" -> Icons.FILE_ICON_SCHEMATIC
            "schem" -> Icons.FILE_ICON_SPONGE_SCH
            "nbt" -> Icons.FILE_ICON_VANILLA
            "json" -> Icons.FILE_ICON_JSON
            "bp" -> GuiIcon.blueprintFile
            else -> GuiIcon.unknownFile
        }
    }

    override fun getIconRoot(): IGuiIcon {
        return Icons.FILE_ICON_DIR_ROOT
    }

    override fun getIconUp(): IGuiIcon {
        return Icons.FILE_ICON_DIR_UP
    }

    override fun getIconCreateDirectory(): IGuiIcon {
        return Icons.FILE_ICON_CREATE_DIR
    }

    override fun getIconSearch(): IGuiIcon {
        return Icons.FILE_ICON_SEARCH
    }

    override fun getIconDirectory(): IGuiIcon {
        return Icons.FILE_ICON_DIR
    }
}