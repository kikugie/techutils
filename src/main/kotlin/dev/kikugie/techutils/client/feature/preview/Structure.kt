package dev.kikugie.techutils.client.feature.preview

import dev.kikugie.techutils.Reference
import dev.kikugie.techutils.client.compat.axiom.BlueprintLoader
import dev.kikugie.techutils.client.feature.preview.util.LitematicaUtils
import dev.kikugie.techutils.client.feature.preview.world.PreviewWorld
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.SchematicaSchematic
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.litematica.util.FileType
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.structure.StructurePlacementData
import net.minecraft.util.math.BlockPos
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Supplier

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
                "bp" -> if (Reference.available("axiom")) BlueprintLoader.fromBlueprint(file, loadPreview) else null
                else -> return null to LoadResult.NOT_SUPPORTED
            }.let {
                return if (it == null) null to LoadResult.FAILURE else it to LoadResult.SUCCESS
            }
        }

        fun fromVanilla(file: File): Structure? {
            val converted = LitematicaSchematic.createFromFile(file.parentFile, file.name, FileType.VANILLA_STRUCTURE)
                ?: return null
            return fromLitematicaInternal(converted, false)
        }

        fun fromSponge(file: File): Structure? {
            val converted = LitematicaSchematic.createFromFile(file.parentFile, file.name, FileType.SPONGE_SCHEMATIC)
                ?: return null
            return fromLitematicaInternal(converted, false)
        }

        fun fromSchematic(file: File): Structure? {
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

        fun fromLitematic(file: File, loadPreview: Boolean): Structure? {
            val litematic =
                LitematicaSchematic.createFromFile(file.parentFile, file.name, FileType.LITEMATICA_SCHEMATIC)
                    ?: return null
            return fromLitematicaInternal(litematic, loadPreview)
        }

        private fun fromLitematicaInternal(
            litematic: LitematicaSchematic, loadPreview: Boolean
        ): Structure {
            val preview = if (loadPreview) LitematicaUtils.loadLitematicaPreview(litematic) else null

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
    }
}