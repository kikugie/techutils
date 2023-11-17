package dev.kikugie.techutils.client.feature.serializer

import dev.kikugie.techutils.client.util.render.TextUtils
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.malilib.gui.GuiTextInputBase
import fi.dy.masa.malilib.gui.Message
import fi.dy.masa.malilib.util.GuiUtils

class ImporterGui(private val placement: SchematicPlacement) : GuiTextInputBase(
    4096,
    "techutils.placement.importGui",
    TextUtils.clipboard ?: "",
    GuiUtils.getCurrentScreen()
) {
    override fun applyValue(string: String): Boolean {
        if (string.isEmpty()) return false
        return if (!PlacementSerializer.deserialize(string, placement)) {
            addMessage(Message.MessageType.ERROR, "techutils.placement.importFail")
            false
        } else true
    }
}