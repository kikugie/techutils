package dev.kikugie.techutils2.impl.structure

import fi.dy.masa.malilib.util.StringUtils
import net.minecraft.util.math.Vec3i
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.properties.Delegates

/**
 * // dev info
 * ## Translations
 * - `techutils.browser.metadata.name(str)`
 * - `techutils.browser.metadata.author(str)`
 * - `techutils.browser.metadata.size(x, y, z)`
 * - `techutils.browser.metadata.blocks(blocks, volume)`
 * - `techutils.browser.metadata.time(time, time)`
 */
class StructureMetadata {
    lateinit var name: String
    lateinit var author: String
    lateinit var size: Vec3i
    var blocks by Delegates.notNull<Int>()
    var volume by Delegates.notNull<Int>()
    var timeCreated by Delegates.notNull<Long>()
    var timeModified by Delegates.notNull<Long>()

    private val template: (String) -> String = { "techutils.browser.metadata.$it" }
    val entries = mapOf(
        "name" to { name },
        "author" to { author },
        "size" to { size },
        "blocks" to { blocks },
        "volume" to { volume },
        "timeCreated" to { timeCreated },
        "timeModified" to { timeModified }
    )
    val translations: Map<String, () -> String> by lazy {
        mapOf(
            "name" to template("name").translation(name),
            "author" to template("author").translation(author),
            "size" to template("size").translation(
                COUNT_FORMAT.format(size.x),
                COUNT_FORMAT.format(size.y),
                COUNT_FORMAT.format(size.z)
            ),
            "blocks" to template("blocks").translation(
                COUNT_FORMAT.format(blocks),
                COUNT_FORMAT.format(volume)
            ),
            "created" to template("created").translation(
                DATE_FORMAT.format(timeCreated)
            ),
            "modified" to template("modified").translation(
                DATE_FORMAT.format(timeModified)
            )
        )
    }

    override fun toString() = entries.map { (k, v) -> "$k=$v" }.joinToString(", ")

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        private val COUNT_FORMAT = DecimalFormat("#,###")

        fun create(init: StructureMetadata.() -> Unit) = StructureMetadata().apply(init)
        fun create(
            name: String,
            author: String,
            size: Vec3i,
            blocks: Int,
            volume: Int,
            timeCreated: Long,
            timeModified: Long,
        ) = create {
            this.name = name
            this.author = author
            this.size = size
            this.blocks = blocks
            this.volume = volume
            this.timeCreated = timeCreated
            this.timeModified = timeModified
        }
    }
}

private fun String.translation(vararg keys: String): () -> String {
    return { StringUtils.translate(this, *keys) }
}
