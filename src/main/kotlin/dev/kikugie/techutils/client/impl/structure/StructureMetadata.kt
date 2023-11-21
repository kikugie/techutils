package dev.kikugie.techutils.client.impl.structure

import net.minecraft.util.math.Vec3i
import java.text.DecimalFormat
import java.text.SimpleDateFormat

data class StructureMetadata(
    val name: String,
    val author: String,
    val size: Vec3i,
    val blocks: Int,
    val volume: Int,
    val timeCreated: Long,
    val timeModified: Long,
) {
    val key = "techutils.browser.metadata"
    val map = linkedMapOf<String, String>(
        "$key.name" to name,
        "$key.author" to author,
        "$key.size" to "${size.x} x ${size.y} x ${size.z}",
        "$key.blocks" to "${dateFormat.format(blocks)}/${dateFormat.format(volume)}",
        "$key.creation" to dateFormat.format(timeCreated),
    )

    class Mutable(
        var name: String?,
        var author: String?,
        var size: Vec3i?,
        var blocks: Int?,
        var volume: Int?,
        var timeCreated: Long?,
        var timeModified: Long?,
    ) {
        fun toImmutable(): StructureMetadata =
            StructureMetadata(name!!, author!!, size!!, blocks!!, volume!!, timeCreated!!, timeModified!!)

        fun isAnyNull(): Boolean =
            listOf(name, author, size, blocks, volume, timeCreated, timeModified).any { it == null }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        private val countFormat = DecimalFormat("#,###")
    }
}
