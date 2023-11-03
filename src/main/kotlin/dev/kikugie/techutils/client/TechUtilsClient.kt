package dev.kikugie.techutils.client

import dev.kikugie.techutils.Reference
import dev.kikugie.techutils.client.config.TUConfig
import dev.kikugie.techutils.client.config.compat.TUMalilibInitializer
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TechUtilsClient : ClientModInitializer {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Reference.MOD_ID)
    }

    override fun onInitializeClient() {
        TUConfig.bootstrap()
        TUMalilibInitializer.init()
    }
}