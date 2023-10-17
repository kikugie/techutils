package dev.kikugie.techutils.client.feature.preview

import net.minecraft.util.math.Vec3i

data class StructureMetadata (
    val name: String,
    val author: String,
    val size: Vec3i,
    val blocks: Int,
    val volume: Int,
    val timeCreated: Long,
    val timeModified: Long,
) {
    val array = arrayOf(name, author, size, blocks, volume, timeCreated, timeModified)
}
