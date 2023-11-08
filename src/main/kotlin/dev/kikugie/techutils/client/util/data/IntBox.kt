package dev.kikugie.techutils.client.util.data

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3i
import kotlin.math.max
import kotlin.math.min
import fi.dy.masa.litematica.selection.Box as LitematicaBox

data class IntBox(
    var x1: Int,
    var y1: Int,
    var z1: Int,
    var x2: Int,
    var y2: Int,
    var z2: Int
) {
    val min = BlockPos(x1, y1, z1)
    val max = BlockPos(x2, y2, z2)
    val size = max.subtract(min).add(1, 1, 1)
    val volume: Int
        get() {
            val size = size
            return size.x * size.y * size.z
        }

    constructor(x: Int, y: Int, z: Int) : this(x, y, z, x, y, z)
    constructor() : this(0, 0, 0, 0, 0, 0)
    constructor(box: Vec3i) : this(box.x, box.y, box.z, box.x, box.y, box.z)
    constructor(pos1: Vec3i, pos2: Vec3i) : this(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z)

    fun toVanillaBox() =
        Box(x1.toDouble(), y1.toDouble(), z1.toDouble(), x2.toDouble() + 1, y2.toDouble() + 1, z2.toDouble() + 1)

    fun toLitematicaBox(name: String = "") = LitematicaBox(min, max, name)

    fun extend(pos: Vec3i) {
        x1 = min(x1, pos.x)
        y1 = min(y1, pos.y)
        z1 = min(z1, pos.z)
        x2 = max(x2, pos.x)
        y2 = max(y2, pos.y)
        z2 = max(z2, pos.z)
    }
}