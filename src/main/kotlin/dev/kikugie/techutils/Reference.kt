package dev.kikugie.techutils

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier


object Reference {
    private val fabric = FabricLoader.getInstance()
    const val MOD_NAME: String = "Technical Utilities"
    const val MOD_ID = "techutils"
    const val VERSION = "1.0.0-dev"
    val isDev = fabric.isDevelopmentEnvironment
    val configPath = fabric.configDir.resolve("$MOD_ID.json")
    fun id(path: String?): Identifier {
        return Identifier(MOD_ID, path)
    }
}
