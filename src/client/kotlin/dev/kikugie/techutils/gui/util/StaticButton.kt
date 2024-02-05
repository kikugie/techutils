package dev.kikugie.techutils.gui.util

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
    /*
    FIXME: disabled texture and offset in 1.20.1-
     */
    override fun getTextureOffset(isMouseOver: Boolean) = 0
}