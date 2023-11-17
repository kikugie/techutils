package dev.kikugie.techutils.client.util.data

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos

fun Long.toBlockPos(): BlockPos = BlockPos.fromLong(this)
fun Long.asChunksPos(): ChunkPos = toBlockPos().asChunkPos()
fun BlockPos.asTriple(): Triple<Int, Int, Int> = Triple(x, y, z)
fun BlockPos.asChunkPos(): ChunkPos = ChunkPos(x shr 4, z shr 4)

fun iterator(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Iterator<BlockPos> {
    return object : Iterator<BlockPos> {
        private var x = x1
        private var y = y1
        private var z = z1
        override fun hasNext(): Boolean {
            return x <= x2 && y <= y2 && z <= z2
        }

        override fun next(): BlockPos {
            val result = BlockPos(x, y, z)
            if (++x > x2) {
                x = x1
                if (++y > y2) {
                    y = y1
                    ++z
                }
            }
            return result
        }
    }
}

fun iterate(x2: Int, y2: Int, z2: Int, x1: Int = 0, y1: Int = 0, z1: Int = 0, action: (BlockPos) -> Unit) {
    iterator(x1, y1, z1, x2, y2, z2).forEach(action)
}