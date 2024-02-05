package dev.kikugie.techutils.impl.structure

import dev.kikugie.techutils.impl.structure.world.StructureWorld
import java.nio.file.Path

class Structure(
    val file: Path? = null,
    val metadata: StructureMetadata,
    worldSupplier: () -> StructureWorld
) {
    val world: StructureWorld by lazy { worldSupplier().also { worldInitialized = true } }
    private var worldInitialized = false

    override fun toString() = """
        file=$file
        metadata=$metadata
        world=${if (worldInitialized) world.toString() else "UNINITIALIZED"}
    """.trimIndent().replace("\n", ", ")
}
