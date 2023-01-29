package me.kikugie.techutils.render.gui

import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.gui.button.ButtonBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import fi.dy.masa.malilib.gui.button.IButtonActionListener
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions
import fi.dy.masa.malilib.util.StringUtils
import me.kikugie.techutils.Reference
import me.kikugie.techutils.config.Configs
import me.kikugie.techutils.render.gui.ConfigGui.GuiTabs.*
import net.minecraft.client.MinecraftClient
import java.util.*

class ConfigGui : GuiConfigsBase(10, 50, Reference.MOD_ID, null, "techutils.gui.config.title", Reference.MOD_VERSION) {
    override fun initGui() {
        super.initGui()
        this.clearOptions()
        val windowWidth = MinecraftClient.getInstance().window.framebufferWidth
        var x = 10
        var y = 26
        var rows = 1
        for (tab in GuiTabs.values()) {
            val width: Int = this.getStringWidth(tab.displayName) + 10
            if (x >= windowWidth - width - 10) {
                x = 10
                y += 22
                rows++
            }
            x += createButton(x, y, width, tab)
        }
        if (rows > 1) {
            val scrollbarPosition: Int = this.listWidget?.scrollbar?.value!!
            this.setListPosition(this.listX, 50 + (rows - 1) * 22)
            this.reCreateListWidget()
            this.listWidget?.scrollbar?.value = scrollbarPosition
            this.listWidget?.refreshEntries()
        }
    }

    override fun getConfigs(): MutableList<ConfigOptionWrapper> {
        val configs: List<IConfigBase> = when (tab) {
            LITEMATICA -> Configs.LITEMATIC_CONFIGS.get()
            WORLDEDIT -> Configs.WORLDEDIT_CONFIGS.get()
            MISC -> Configs.MISC_CONFIGS.get()
        }
        return ConfigOptionWrapper.createFor(configs)
    }

    private fun createButton(x: Int, y: Int, width: Int, tab: GuiTabs): Int {
        val button = ButtonGeneric(x, y, width, 20, tab.displayName)
        button.setEnabled(Companion.tab != tab)
        this.addButton(button, ButtonListener(tab, this))
        return button.width + 2
    }

    enum class GuiTabs(private val translationKey: String) {
        LITEMATICA("techutils.gui.config.tab.litematica"),
        WORLDEDIT("techutils.gui.config.tab.worldedit"),
        MISC("techutils.gui.config.tab.misc");

        val displayName: String
            get() = StringUtils.translate(translationKey)
    }

    internal data class ButtonListener(val tab: GuiTabs, val parent: ConfigGui) : IButtonActionListener {
        override fun actionPerformedWithButton(button: ButtonBase, mouseButton: Int) {
            Companion.tab = this.tab
            parent.reCreateListWidget() // apply the new config width
            Objects.requireNonNull<WidgetListConfigOptions>(parent.listWidget).resetScrollbarPosition()
            parent.initGui()
        }
    }

    companion object {
        private var tab = LITEMATICA
    }
}