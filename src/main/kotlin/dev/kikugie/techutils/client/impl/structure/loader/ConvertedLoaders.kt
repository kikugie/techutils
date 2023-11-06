package dev.kikugie.techutils.client.impl.structure.loader

import dev.kikugie.techutils.client.impl.structure.Structure
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.util.FileType
import java.nio.file.Path

object SpongeLoader : StructureLoader {
    override val format = "schem"

    override fun load(file: Path, lazy: Boolean): Structure {
        val converted = LitematicaSchematic.createFromFile(
            file.parent.toFile(),
            file.fileName.toString(),
            FileType.SPONGE_SCHEMATIC
        ) ?: throw loadException(file, "Failed to load schem file")
        return LitematicLoader.loadInternal(file, converted, lazy, false)
    }
}

object SchematicLoader : StructureLoader {
    override val format = "schematic"

    override fun load(file: Path, lazy: Boolean): Structure {
        val converted = LitematicaSchematic.createFromFile(
            file.parent.toFile(),
            file.fileName.toString(),
            FileType.SCHEMATICA_SCHEMATIC
        ) ?: throw loadException(file, "Failed to load schematic file")
        return LitematicLoader.loadInternal(file, converted, lazy, false)
    }
}

object NbtLoader : StructureLoader {
    override val format = "schematic"

    override fun load(file: Path, lazy: Boolean): Structure {
        val converted = LitematicaSchematic.createFromFile(
            file.parent.toFile(),
            file.fileName.toString(),
            FileType.VANILLA_STRUCTURE
        ) ?: throw loadException(file, "Failed to load nbt structure file")
        return LitematicLoader.loadInternal(file, converted, lazy, false)
    }
}