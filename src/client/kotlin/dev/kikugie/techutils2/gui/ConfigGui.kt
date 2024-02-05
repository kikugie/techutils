package dev.kikugie.techutils2.gui

import dev.kikugie.malilib_extras.api.config.ConfigCategory
import dev.kikugie.malilib_extras.api.config.ConfigEntry
import dev.kikugie.malilib_extras.api.config.MalilibConfig
import dev.kikugie.malilib_extras.util.translate
import dev.kikugie.techutils.Reference
import dev.kikugie.techutils2.TechUtilsClient
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.gui.button.ButtonBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import net.minecraft.client.gui.screen.Screen


class ConfigGui(
    config: MalilibConfig,
    parent: Screen?
) : GuiConfigsBase(
    10,
    50,
    Reference.MOD_ID,
    parent,
    "techutils.title",
    Reference.VERSION
) {
    private var tab = config.categories.first()
    private val options: List<ConfigEntry>
        get() = TechUtilsClient.config.groups[tab.id] ?: emptyList()

    override fun initGui() {
        super.initGui()
        clearOptions()

        var x = 10
        val y = 26

        TechUtilsClient.config.categories.forEach {
            x += createNavigationButton(x, y, it)
        }
    }

    private fun createNavigationButton(x: Int, y: Int, category: ConfigCategory): Int {
        val button = ButtonGeneric(x, y, -1, 20, category.name.translate())
        button.setEnabled(tab != category)
        button.setHoverStrings(category.description)
        addButton(
            button
        ) { _: ButtonBase?, _: Int ->
            refresh(
                tab,
                category
            ) { tab = category }
        }
        return button.width + 2
    }

    private fun <T> refresh(currentValue: T, newValue: T, valueSetter: Runnable) {
        if (newValue != currentValue) {
            valueSetter.run()
            reCreateListWidget()
            listWidget?.resetScrollbarPosition()
            initGui()
        }
    }

    override fun getConfigs(): MutableList<ConfigOptionWrapper> =
        options.map { ConfigOptionWrapper(it.config) }.toMutableList()
}