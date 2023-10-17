package dev.kikugie.techutils

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier

object Reference {
    const val MOD_ID = "techutils"
    fun id(path: String?): Identifier {
        return Identifier(MOD_ID, path)
    }

    fun available(mod: String): Boolean {
        val fabric = FabricLoader.getInstance()
        return if (fabric.isDevelopmentEnvironment) true else fabric.isModLoaded(mod)
    }
}
