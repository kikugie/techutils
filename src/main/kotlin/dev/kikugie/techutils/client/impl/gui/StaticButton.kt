package dev.kikugie.techutils.client.impl.gui

import fi.dy.masa.malilib.gui.LeftRight
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon

class StaticButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    text: String,
    icon: IGuiIcon,
    vararg hoverStrings: String
) :
    ButtonGeneric(x, y, width, height, text, icon, *hoverStrings) {
    init {
        alignment = LeftRight.CENTER
    }

    override fun getTextureOffset(isMouseOver: Boolean) = 0
}