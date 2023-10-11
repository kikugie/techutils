package dev.kikugie.techutils.client

import dev.kikugie.techutils.Reference
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TechUtilsClient : ClientModInitializer {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Reference.MOD_ID)
    }

    override fun onInitializeClient() {
    }
}