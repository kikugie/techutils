package dev.kikugie.techutils2.gui

import dev.kikugie.techutils2.impl.serializer.PlacementSerializer
import dev.kikugie.techutils2.util.TextUtils
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.malilib.gui.GuiTextInputBase
import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.util.GuiUtils

/**
 * // dev info
 * ## Translations
 * - `techutils.placement.gui_title()`
 * - `techutils.placement.import_fail()`
 */
class ImporterGui(private val placement: SchematicPlacement) : GuiTextInputBase(
    4096,
    "techutils.placement.gui_title",
    TextUtils.clipboard ?: "",
    GuiUtils.getCurrentScreen()
) {
    override fun applyValue(string: String) = when {
        string.isBlank() -> false
        PlacementSerializer.deserialize(string, placement) -> true
        else -> {
            addMessage(Message.MessageType.ERROR, "techutils.placement.import_fail")
            false
        }
    }
}