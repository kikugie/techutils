package dev.kikugie.techutils.client.feature.browser.metadata

import net.minecraft.util.math.Vec3i
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
        "$key.blocks" to "$blocks/$volume",
        "$key.creation" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timeCreated),
    )

}
