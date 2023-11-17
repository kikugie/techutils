package dev.kikugie.techutils.client.impl.structure.loader

import dev.kikugie.techutils.client.impl.structure.Structure
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.util.FileType
import fi.dy.masa.litematica.util.WorldUtils
import fi.dy.masa.malilib.interfaces.IStringConsumer
import java.nio.file.Path
import kotlin.io.path.name

object SpongeLoader : StructureLoader {
    override val format = "schem"

    override fun load(file: Path, lazy: Boolean): Structure {
        val converted = LitematicaSchematic.createFromFile(
            file.parent.toFile(),
            file.fileName.toString(),
            FileType.SPONGE_SCHEMATIC
        ) ?: throw loadException(file, "Invalid schem file")
        return LitematicLoader.loadInternal(file, converted, lazy, false)
    }

    override fun toLitematic(file: Path, feedback: IStringConsumer): LitematicaSchematic {
        return WorldUtils.convertSpongeSchematicToLitematicaSchematic(file.parent.toFile(), file.name)
            ?: throw loadException(file, "Failed to convert schematic")
    }
}

object SchematicLoader : StructureLoader {
    override val format = "schematic"

    override fun load(file: Path, lazy: Boolean): Structure {
        val converted = LitematicaSchematic.createFromFile(
            file.parent.toFile(),
            file.fileName.toString(),
            FileType.SCHEMATICA_SCHEMATIC
        ) ?: throw loadException(file, "Invalid schematic file")
        return LitematicLoader.loadInternal(file, converted, lazy, false)
    }

    override fun toLitematic(file: Path, feedback: IStringConsumer): LitematicaSchematic {
        return WorldUtils.convertSchematicaSchematicToLitematicaSchematic(
            file.parent.toFile(),
            file.name,
            false,
            feedback
        ) ?: throw loadException(file, "Failed to convert schematic")
    }
}

object NbtLoader : StructureLoader {
    override val format = "schematic"

    override fun load(file: Path, lazy: Boolean): Structure {
        val converted = LitematicaSchematic.createFromFile(
            file.parent.toFile(),
            file.fileName.toString(),
            FileType.VANILLA_STRUCTURE
        ) ?: throw loadException(file, "Invalid nbt structure file")
        return LitematicLoader.loadInternal(file, converted, lazy, false)
    }

    override fun toLitematic(file: Path, feedback: IStringConsumer): LitematicaSchematic {
        return WorldUtils.convertStructureToLitematicaSchematic(file.parent.toFile(), file.name) ?: throw loadException(
            file,
            "Failed to convert schematic"
        )
    }
}