package dev.kikugie.techutils.util

import net.fabricmc.loader.api.FabricLoader

val FABRIC = FabricLoader.getInstance()

fun isLoaded(mod: String) = FABRIC.isModLoaded(mod)