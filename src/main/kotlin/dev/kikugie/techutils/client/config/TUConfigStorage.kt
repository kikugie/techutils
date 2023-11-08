package dev.kikugie.techutils.client.config

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.kikugie.techutils.Reference
import dev.kikugie.techutils.client.TechUtilsClient
import fi.dy.masa.malilib.config.ConfigUtils
import fi.dy.masa.malilib.config.IConfigHandler
import fi.dy.masa.malilib.util.FileUtils
import fi.dy.masa.malilib.util.JsonUtils
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
            ConfigUtils.readConfigBase(json, "techutils", TUConfig.options.map { it.config })
        } catch (e: Exception) {
            TechUtilsClient.LOGGER.warn("Failed to parse config file: ${e.message}")
        }
    }

    /**
     * Called to save any potential config changes to a file
     */
    override fun save() {
        val dir = FileUtils.getConfigDirectory()

        if (dir.exists() && dir.isDirectory() || dir.mkdirs()) {
            val root = JsonObject()
            ConfigUtils.writeConfigBase(root, "techutils", TUConfig.options.map { it.config })
            JsonUtils.writeJsonToFile(root, Reference.configPath.toFile())
        }
    }
}