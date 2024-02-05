package dev.kikugie.techutils2

import dev.kikugie.malilib_extras.api.config.ConfigRegistry
import dev.kikugie.malilib_extras.api.config.MalilibConfig
import dev.kikugie.techutils.Reference
import dev.kikugie.techutils2.config.BrowserConfig
import dev.kikugie.techutils2.config.GeneralMisc
import dev.kikugie.techutils2.config.LitematicaMisc
import dev.kikugie.techutils2.gui.ConfigGui
import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object TechUtilsClient : ClientModInitializer {
    val LOGGER = LoggerFactory.getLogger("Tech Utils")
    lateinit var config: MalilibConfig
        private set

    override fun onInitializeClient() {
        config = MalilibConfig.create(Reference.MOD_ID, Reference.VERSION) {
            register(BrowserConfig::class)
            register(LitematicaMisc::class)
            register(GeneralMisc::class)
        }
        ConfigRegistry.register(config, modmenu = true) {ConfigGui(config,it)}
    }
}