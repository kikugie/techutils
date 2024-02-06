package dev.kikugie.techutils.impl.worldedit

import net.minecraft.util.math.BlockPos
import kotlin.properties.Delegates

data class WorldEditStorage(
    var pos1: BlockPos? = null,
    var pos2: BlockPos? = null,
) {
    var cuboid: Boolean by Delegates.observable(false) { _, _, it ->
        if (!it) {
            pos1 = null
            pos2 = null
        }
    }
}
