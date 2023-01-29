package me.kikugie.techutils.render.litematica

import com.mojang.blaze3d.systems.RenderSystem
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase
import fi.dy.masa.malilib.config.IConfigOptionListEntry
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase
import me.kikugie.techutils.config.Configs
import net.minecraft.client.MinecraftClient

class LitematicRenderManager private constructor(gui: GuiSchematicBrowserBase) {
    private val rendererCache: MutableMap<WidgetFileBrowserBase.DirectoryEntry, LitematicRenderer> =
        HashMap()
    private val gui: GuiSchematicBrowserBase?
    private val slant: Int
    private var currentRenderer: LitematicRenderer? = null
    private var renderX = 0
    private var renderY = 0
    private var viewportSize = 0
    private var validDragging = false

    init {
        this.gui = gui
        slant = Configs.LitematicConfigs.RENDER_SLANT.integerValue
    }

    fun setCurrentRenderer(entry: WidgetFileBrowserBase.DirectoryEntry?) {
        if (gui == null) return
        if (rendererCache.containsKey(entry)) {
            currentRenderer = rendererCache[entry]
        } else {
            val renderer = LitematicRenderer(entry!!, gui, slant)
            rendererCache[entry] = renderer
            currentRenderer = renderer
        }
    }

    fun renderCurrent(x: Int, y: Int, viewportSize: Int) {
        if (currentRenderer == null) return
        renderX = x
        renderY = y
        this.viewportSize = viewportSize
        if (Configs.LitematicConfigs.RENDER_ROTATION_MODE.stringValue.equals(RotationMode.FREE_SPIN.displayName)) {
            val mouseX = MinecraftClient.getInstance().mouse.x
            val windowWidth = MinecraftClient.getInstance().window.framebufferWidth
            currentRenderer!!.angle = (mouseX / windowWidth * 360).toInt()
        }
        currentRenderer!!.render(RenderSystem.getModelViewStack(), x, y, viewportSize)
    }

    fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double) {
        if (!Configs.LitematicConfigs.RENDER_ROTATION_MODE.stringValue
                .equals(RotationMode.SCROLL.displayName) || !isInViewPort(mouseX, mouseY)
        ) return
        currentRenderer!!.angle += (amount * 10).toInt()
    }

    fun mouseDragged(deltaX: Double) {
        if (!Configs.LitematicConfigs.RENDER_ROTATION_MODE.stringValue
                .equals(RotationMode.DRAG.displayName) || !validDragging
        ) return
        currentRenderer!!.angle += (deltaX * 2).toInt()
    }

    fun mouseReleased() {
        validDragging = false
    }

    fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        if (isInViewPort(mouseX, mouseY) && mouseButton == 0) {
            validDragging = true
        }
    }

    private fun isInViewPort(mouseX: Double, mouseY: Double): Boolean {
        return mouseX > renderX && mouseY > renderY && mouseX < renderX + viewportSize && mouseY < renderY + viewportSize
    }

    enum class RotationMode(private val displayName: String) : IConfigOptionListEntry {
        FREE_SPIN("Free Spin"), DRAG("Drag"), SCROLL("Scroll");

        override fun getStringValue(): String {
            return displayName
        }

        override fun getDisplayName(): String {
            return displayName
        }

        override fun cycle(forward: Boolean): IConfigOptionListEntry {
            val mod = if (forward) 1 else -1
            return values()[(ordinal + mod) % values().size]
        }

        override fun fromString(value: String): IConfigOptionListEntry {
            val vals = values()
            for (temp in vals) {
                if (temp.displayName.equals(value, ignoreCase = true)) {
                    return temp
                }
            }
            return DRAG
        }
    }

    companion object {
        @JvmStatic
        var instance: LitematicRenderManager? = null
            private set

        fun init(gui: GuiSchematicBrowserBase): LitematicRenderManager? {
            instance = LitematicRenderManager(gui)
            return instance
        }

        @JvmStatic
        fun reset() {
            instance = null
        }
    }
}