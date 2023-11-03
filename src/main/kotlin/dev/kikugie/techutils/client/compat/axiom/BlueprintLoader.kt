package dev.kikugie.techutils.client.compat.axiom

import com.moulberry.axiom.blueprint.Blueprint
import com.moulberry.axiom.blueprint.BlueprintIo
import dev.kikugie.techutils.client.feature.browser.metadata.Structure
import dev.kikugie.techutils.client.feature.browser.metadata.StructureMetadata
import dev.kikugie.techutils.client.feature.browser.preview.world.PreviewWorld
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.LitematicaSchematic.SchematicSaveInfo
import fi.dy.masa.litematica.selection.AreaSelection
import fi.dy.masa.litematica.selection.Box
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase.DirectoryEntry
import fi.dy.masa.malilib.interfaces.IStringConsumer
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

object BlueprintLoader {
    fun fromBlueprint(file: File, loadPreview: Boolean): Structure? {
        val blueprint: Blueprint
        try {
            blueprint = BlueprintIo.readBlueprint(file.inputStream())
        } catch (e: IOException) {
            return null
        }
        val preview = if (loadPreview) blueprint.thumbnail() else null

        val region = blueprint.blockRegion()
        val size = Vec3i(
            region.max()!!.x - region.min()!!.x,
            region.max()!!.y - region.min()!!.y,
            region.max()!!.z - region.min()!!.z
        )
        val attrs = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)

        val header = blueprint.header()
        val metadata = StructureMetadata(
            header.name,
            header.author,
            size,
            header.blockCount,
            size.x * size.y * size.z,
            attrs.creationTime().toMillis(),
            attrs.lastModifiedTime().toMillis()
        )

        return Structure(metadata, { createAxiomWorld(blueprint) }, preview)
    }

    @JvmStatic
    fun toLitematic(file: File, feedback: IStringConsumer): LitematicaSchematic? {
        val blueprint: Blueprint
        try {
            blueprint = BlueprintIo.readBlueprint(file.inputStream())
        } catch (e: IOException) {
            return null
        }

        val world = createAxiomWorld(blueprint)

        val header = blueprint.header()
        val region = blueprint.blockRegion()
        val size = BlockPos(
            region.max()!!.x - region.min()!!.x,
            region.max()!!.y - region.min()!!.y,
            region.max()!!.z - region.min()!!.z
        )

        val selection = AreaSelection()
        selection.addSubRegionBox(Box(BlockPos.ORIGIN, size, header.name), false)
        val info = SchematicSaveInfo(false, false)

        val litematic = LitematicaSchematic.createFromWorld(world, selection, info, header.author, feedback)
        litematic?.metadata?.name = header.name
        return litematic
    }

    @JvmStatic
    fun isBlueprint(entry: DirectoryEntry): Boolean {
        return entry.fullPath.extension == "bp"
    }

    private fun createAxiomWorld(blueprint: Blueprint): PreviewWorld {
        val world = PreviewWorld(MinecraftClient.getInstance())
        val min = blueprint.blockRegion().min()!!
        blueprint.blockRegion().forEachEntry { x, y, z, state ->
            world.setBlockState(BlockPos(x - min.x, y - min.y, z - min.z), state)
        }
        return world
    }
}