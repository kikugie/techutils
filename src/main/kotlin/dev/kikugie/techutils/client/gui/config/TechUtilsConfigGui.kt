package dev.kikugie.techutils.client.gui.config

import dev.kikugie.techutils.Reference
import dev.kikugie.techutils.client.config.ConfigCategory
import dev.kikugie.techutils.client.gui.icon.GuiIcon
import fi.dy.masa.malilib.gui.GuiConfigsBase
import fi.dy.masa.malilib.gui.button.ButtonBase
import fi.dy.masa.malilib.gui.button.ButtonGeneric
import net.minecraft.client.gui.screen.Screen


class TechUtilsConfigGui(parent: Screen?) : GuiConfigsBase(
    10,
    50,
    Reference.MOD_ID,
    parent,
    "techutils.title",
    Reference.VERSION
) {
    private var tab = ConfigCategory.categories[0]

    override fun initGui() {
        super.initGui()
        clearOptions()

        var x = 10
        val y = 26

        ConfigCategory.categories.forEach {
            x += createNavigationButton(x, y, it)
        }
    }

    private fun createNavigationButton(x: Int, y: Int, category: ConfigCategory): Int {
        val button = ButtonGeneric(x, y, -1, 20, category.displayName)
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
        tab.options.map { ConfigOptionWrapper(it.config) }.toMutableList()


    data class GuiTab(
        val name: String,
        val icon: GuiIcon,
    )
}