package dev.kikugie.techutils.client.config

import com.google.gson.JsonParser
import dev.kikugie.techutils.Reference
import dev.kikugie.techutils.client.TechUtilsClient
import fi.dy.masa.malilib.config.IConfigHandler
import kotlin.io.path.exists
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.reader

class TUConfigStorage : IConfigHandler {
    /**
     * Called after game launch to load the configs from file
     */
    override fun load() {
        val file = Reference.configPath
        if (!file.exists() || !file.isRegularFile() || !file.isReadable())
            return
        try {
            val json = JsonParser.parseReader(file.reader()).asJsonObject
        } catch (e: Exception) {
            TechUtilsClient.LOGGER.warn("Failed to parse config file: ${e.message}")
        }
    }

    /**
     * Called to save any potential config changes to a file
     */
    override fun save() {
        TODO("Not yet implemented")
    }
}