package dev.kikugie.techutils.client.config

import dev.kikugie.techutils.client.config.option.TUOption
import fi.dy.masa.malilib.util.StringUtils

class ConfigCategory(
    key: String
) {
    val name = key.lowercase()
    val options: MutableList<TUOption> = mutableListOf()
    val displayName: String
        get() = StringUtils.translate("techutils.category.$name")
    val description: String
        get() = StringUtils.translate("techutils.category.$name.desc")
    companion object {
        val LITEMATICA = ConfigCategory("litematica")
        val WORLDEDIT = ConfigCategory("worldedit")
        val MISC = ConfigCategory("misc")

        val categories = listOf(LITEMATICA, WORLDEDIT, MISC)
    }
}
