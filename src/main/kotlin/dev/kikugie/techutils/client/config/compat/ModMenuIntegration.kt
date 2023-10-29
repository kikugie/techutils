package dev.kikugie.techutils.client.config.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.kikugie.techutils.client.gui.config.TechUtilsConfigGui
import net.minecraft.client.gui.screen.Screen


class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> =
        ConfigScreenFactory<Screen> { screen: Screen? -> TechUtilsConfigGui(screen) }
}