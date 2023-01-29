package me.kikugie.techutils.networking

import net.minecraft.util.math.BlockPos

class WorldEditStorage {
    private val region = arrayOfNulls<BlockPos>(2)
    var isCuboidMode = false
        private set

    fun setPos(n: Int, pos: BlockPos?) {
        region[n] = pos
    }

    fun getPos(n: Int): BlockPos? {
        return region[n]
    }

    val isComplete: Boolean
        get() = region[0] != null && region[1] != null

    fun parseMode(mode: String) {
        isCuboidMode = mode == "cuboid"
    }
}