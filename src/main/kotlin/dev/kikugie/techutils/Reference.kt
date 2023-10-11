package dev.kikugie.techutils

import net.minecraft.util.Identifier

object Reference {
    const val MOD_ID = "techutils"
    fun id(path: String?): Identifier {
        return Identifier(MOD_ID, path)
    }
}
