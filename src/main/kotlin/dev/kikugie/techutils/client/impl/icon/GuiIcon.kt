package dev.kikugie.techutils.client.impl.icon

import dev.kikugie.techutils.Reference
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon
import fi.dy.masa.malilib.render.RenderUtils
import net.minecraft.util.Identifier

data class GuiIcon(
    @JvmField val texture: Identifier,
    @JvmField val u: Int,
    @JvmField val v: Int,
    @JvmField val width: Int,
    @JvmField val height: Int
) : IGuiIcon {
    override fun getWidth(): Int {
        return width
    }

    override fun getHeight(): Int {
        return height
    }

    override fun getU(): Int {
        return u
    }

    override fun getV(): Int {
        return v
    }

    override fun renderAt(x: Int, y: Int, zLevel: Float, enabled: Boolean, selected: Boolean) {
        RenderUtils.drawTexturedRect(x, y, u, v, width, height, zLevel)
    }

    override fun getTexture(): Identifier {
        return texture
    }

    companion object {
        val WIDGETS = Reference.id("textures/gui/widgets.png")

        val blueprintFile = GuiIcon(WIDGETS, 24, 0, 12, 12)
        val errorFile = GuiIcon(WIDGETS, 12, 0, 12, 12)
        val unknownFile = GuiIcon(WIDGETS, 0, 0, 12, 12)

        val export = GuiIcon(WIDGETS, 0, 12, 16, 16)
        val import = GuiIcon(WIDGETS, 16, 12, 16, 16)
    }
}