package dev.kikugie.techutils.client.util

import me.fallenbreath.conditionalmixin.api.util.VersionChecker
import net.fabricmc.loader.api.FabricLoader

object FabricUtils {
    fun isLoaded(mod: String): Boolean {
        val fabric = FabricLoader.getInstance()
        return if (fabric.isDevelopmentEnvironment) true else fabric.isModLoaded(mod)
    }

    fun fitsAnyPredicate(mod: String, versionPredicates: Collection<String>): Boolean {
        return FabricLoader.getInstance().getModContainer(mod).map {
            VersionChecker.doesVersionSatisfyPredicate(
                it.metadata.version,
                versionPredicates
            )
        }.orElse(false)
    }

    fun fitsAnyPredicate(mod: String, versionPredicate: String): Boolean {
        return fitsAnyPredicate(mod, listOf(versionPredicate))
    }
}