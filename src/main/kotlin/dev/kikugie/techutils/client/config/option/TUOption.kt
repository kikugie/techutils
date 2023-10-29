package dev.kikugie.techutils.client.config.option

import dev.kikugie.techutils.client.config.annotation.Settings
import dev.kikugie.techutils.client.util.condition.ModRestriction
import fi.dy.masa.malilib.config.IConfigBase

data class TUOption(
    val config: IConfigBase,
    val settings: Settings
) {
    val modRestriction = ModRestriction.of(settings.restriction)
    val debug = settings.debug
    val dev = settings.dev
}