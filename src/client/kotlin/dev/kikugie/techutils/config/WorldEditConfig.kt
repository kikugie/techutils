package dev.kikugie.techutils.config

import dev.kikugie.malilib_extras.api.config.Category
import fi.dy.masa.malilib.config.options.*

@Suppress("unused")
@Category(LITEMATICA)
object WorldEditConfig {
    val sync = FACTORY.create<ConfigBooleanHotkeyed>("worldEditSync", true, "") {}
    val syncTicks = FACTORY.create<ConfigInteger>("worldEditSyncTicks", 20) {
        slider = false
        min = 1
        max = 1000
    }
    val syncFeedback = FACTORY.create<ConfigBoolean>("worldEditSyncFeedback", true)
    val disableUpdates = FACTORY.create<ConfigBoolean>("worldEditPerfOff", true)
}