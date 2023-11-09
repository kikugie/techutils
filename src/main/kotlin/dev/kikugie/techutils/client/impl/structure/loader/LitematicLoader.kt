package dev.kikugie.techutils.client.impl.structure.loader

import dev.kikugie.techutils.client.TechUtilsClient
import dev.kikugie.techutils.client.impl.structure.Structure
import dev.kikugie.techutils.client.impl.structure.StructureMetadata
import dev.kikugie.techutils.client.impl.structure.world.StructureWorld
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.litematica.util.FileType
import fi.dy.masa.malilib.interfaces.IStringConsumer
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.math.BlockPos
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.math.sqrt

object LitematicLoader : StructureLoader {
    override val format = "litematic"
    override fun load(file: Path, lazy: Boolean): Structure {
        val litematic = LitematicaSchematic.createFromFile(
            file.parent.toFile(),
            file.fileName.toString(),
            FileType.LITEMATICA_SCHEMATIC
        ) ?: throw loadException(file, "Failed to load litematic file")
        return loadInternal(file, litematic, lazy)
    }

    override fun toLitematic(file: Path, feedback: IStringConsumer): LitematicaSchematic {
        return LitematicaSchematic.createFromFile(file.parent.toFile(), file.name, FileType.LITEMATICA_SCHEMATIC) ?: throw loadException(file, "Invalid litematic")
    }

    internal fun loadInternal(
        file: Path,
        litematic: LitematicaSchematic,
        lazy: Boolean,
        loadPreview: Boolean = true
    ): Structure {
        val preview = if (loadPreview) try {
            loadLitematicaPreview(litematic)
        } catch (e: Exception) {
            TechUtilsClient.LOGGER.warn(e.message)
            null
        } else null
        val metadata = StructureMetadata(
            litematic.metadata.name,
            litematic.metadata.author,
            litematic.totalSize,
            litematic.metadata.totalBlocks,
            litematic.metadata.totalVolume,
            litematic.metadata.timeCreated,
            litematic.metadata.timeModified
        )
        return if (lazy) Structure(metadata, preview, file) { createWorld(litematic) }
        else Structure(metadata, preview, file, createWorld(litematic))
    }

    private fun createWorld(litematic: LitematicaSchematic): StructureWorld {
        val world = StructureWorld()
        val placement = SchematicPlacement.createTemporary(litematic, BlockPos.ORIGIN)
        litematic.placeToWorld(world, placement, false, false)
        return world
    }

    private fun loadLitematicaPreview(litematic: LitematicaSchematic): NativeImageBackedTexture? {
        val file = litematic.file?.toPath() ?: Path.of(litematic.metadata.name)

        val pixelData = litematic.metadata.previewImagePixelData ?: return null
        if (pixelData.isEmpty()) return null

        val path =
            if (litematic.file != null) litematic.file!!.absolutePath else "${litematic.metadata.name} (in-memory)"
        val size = sqrt(pixelData.size.toDouble()).toInt()
        if (size * size != pixelData.size) throw loadException(file, "Invalid preview image size")
        return try {
            val image = NativeImage(size, size, false)
            val texture = NativeImageBackedTexture(image)
            for (y in 0 until size) for (x in 0 until size) {
                val pixel = pixelData[y * size + x]
                image.setColor(
                    x, y, pixel and -0xff0100 or (pixel and 0xFF0000 shr 16) or (pixel and 0xFF shl 16)
                )
            }
            texture
        } catch (e: Exception) {
            throw loadException(file, "Failed to load write preview image")
        }
    }
}