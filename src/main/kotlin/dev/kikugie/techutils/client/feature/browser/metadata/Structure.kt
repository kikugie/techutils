package dev.kikugie.techutils.client.feature.browser.metadata

import dev.kikugie.techutils.Reference
import dev.kikugie.techutils.client.TechUtilsClient
import dev.kikugie.techutils.client.compat.axiom.BlueprintLoader
import dev.kikugie.techutils.client.feature.preview.world.PreviewWorld
import dev.kikugie.techutils.client.util.FabricUtils
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.SchematicaSchematic
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.litematica.util.FileType
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.structure.StructurePlacementData
import net.minecraft.util.math.BlockPos
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Supplier
import kotlin.math.sqrt

class Structure(
    val metadata: StructureMetadata,
    private val worldProvider: Supplier<PreviewWorld>,
    val preview: NativeImageBackedTexture? = null
) {
    private var cachedWorld: PreviewWorld? = null
    val world: PreviewWorld
        get() {
            if (cachedWorld == null) cachedWorld = worldProvider.get()
            return cachedWorld!!
        }

    enum class LoadResult {
        SUCCESS,
        FAILURE,
        NOT_SUPPORTED
    }

    companion object {
        fun from(entry: DirectoryEntry, loadPreview: Boolean): Pair<Structure?, LoadResult> {
            val extension = com.google.common.io.Files.getFileExtension(entry.name)
            val file = entry.fullPath
            when (extension) {
                "nbt" -> fromVanilla(file)
                "schem" -> fromSponge(file)
                "schematic" -> fromSchematic(file)
                "litematic" -> fromLitematic(file, loadPreview)
                "bp" -> if (FabricUtils.isLoaded("axiom")) BlueprintLoader.fromBlueprint(file, loadPreview) else null
                else -> return null to LoadResult.NOT_SUPPORTED
            }.let {
                return if (it == null) null to LoadResult.FAILURE else it to LoadResult.SUCCESS
            }
        }

        private fun fromVanilla(file: File): Structure? {
            val converted = LitematicaSchematic.createFromFile(file.parentFile, file.name, FileType.VANILLA_STRUCTURE)
                ?: return null
            return fromLitematicaInternal(converted, false)
        }

        private fun fromSponge(file: File): Structure? {
            val converted = LitematicaSchematic.createFromFile(file.parentFile, file.name, FileType.SPONGE_SCHEMATIC)
                ?: return null
            return fromLitematicaInternal(converted, false)
        }

        private fun fromSchematic(file: File): Structure? {
            val schematic = SchematicaSchematic.createFromFile(file) ?: return null

            val world = PreviewWorld(MinecraftClient.getInstance())
            schematic.placeSchematicDirectlyToChunks(world, BlockPos.ORIGIN, StructurePlacementData())

            val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
            val metadata = StructureMetadata(
                file.nameWithoutExtension,
                "Unknown",
                schematic.size,
                world.blocks,
                schematic.size.x * schematic.size.y * schematic.size.z,
                attrs.creationTime().toMillis(),
                attrs.lastModifiedTime().toMillis()
            )

            return Structure(metadata, { world })
        }

        private fun fromLitematic(file: File, loadPreview: Boolean): Structure? {
            val litematic =
                LitematicaSchematic.createFromFile(file.parentFile, file.name, FileType.LITEMATICA_SCHEMATIC)
                    ?: return null
            return fromLitematicaInternal(litematic, loadPreview)
        }

        private fun fromLitematicaInternal(
            litematic: LitematicaSchematic, loadPreview: Boolean
        ): Structure {
            val preview = if (loadPreview) loadLitematicaPreview(litematic) else null

            val metadata = StructureMetadata(
                litematic.metadata.name,
                litematic.metadata.author,
                litematic.totalSize,
                litematic.metadata.totalBlocks,
                litematic.metadata.totalVolume,
                litematic.metadata.timeCreated,
                litematic.metadata.timeModified
            )
            return Structure(metadata, {
                val world = PreviewWorld(MinecraftClient.getInstance())
                val placement = SchematicPlacement.createTemporary(litematic, BlockPos.ORIGIN)
                litematic.placeToWorld(world, placement, false, false)
                return@Structure world
            }, preview)
        }

        private fun loadLitematicaPreview(litematic: LitematicaSchematic): NativeImageBackedTexture? {
            val pixelData = litematic.metadata.previewImagePixelData ?: return null
            if (pixelData.isEmpty()) return null

            val path =
                if (litematic.file != null) litematic.file!!.absolutePath else "${litematic.metadata.name} (in-memory)"
            val size = sqrt(pixelData.size.toDouble()).toInt()
            if (size * size != pixelData.size) return null
            try {
                val image = NativeImage(size, size, false)
                val texture = NativeImageBackedTexture(image)
                val id = Reference.id(DigestUtils.sha1Hex(path))
                MinecraftClient.getInstance().textureManager.registerTexture(id, texture)
                for (y in 0 until size) for (x in 0 until size) {
                    val pixel = pixelData[y * size + x]
                    image.setColor(
                        x, y, pixel and -0xff0100 or (pixel and 0xFF0000 shr 16) or (pixel and 0xFF shl 16)
                    )
                }
                texture.upload()
                return texture
            } catch (e: Exception) {
                TechUtilsClient.LOGGER.warn("Failed to load preview image for schematic $path", e)
                return null
            }
        }
    }
}