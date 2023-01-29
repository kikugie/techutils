package me.kikugie.techutils.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.kikugie.techutils.render.gui.ConfigGui
import net.minecraft.client.gui.screen.Screen

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory<Screen> { screen: Screen? ->
            val gui = ConfigGui()
            gui.parent = screen
            gui
        }
    }
}