package dev.kikugie.techutils.config

import dev.kikugie.malilib_extras.api.config.Category
import dev.kikugie.techutils.TechUtilsClient
import dev.kikugie.techutils.gui.ConfigGui
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.gui.GuiBase

@Category(MISC)
object GeneralMisc {
    val openConfig = FACTORY.create<ConfigHotkey>("openConfig", "X, O") {
        callback = {
            GuiBase.openGui(ConfigGui(TechUtilsClient.config, null))
            true
        }
    }
    val compactScoreboard = FACTORY.create<ConfigBooleanHotkeyed>("compactScoreboard", false, "F6") {}
}