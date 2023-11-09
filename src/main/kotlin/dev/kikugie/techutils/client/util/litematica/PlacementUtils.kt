package dev.kikugie.techutils.client.util.litematica

import dev.kikugie.techutils.client.util.WorldUtils
import dev.kikugie.techutils.client.util.multiversion.getPlacementsTouching
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.litematica.util.PositionUtils
import fi.dy.masa.litematica.util.SchematicUtils
import net.minecraft.block.entity.BlockEntity
import net.minecraft.inventory.Inventory
import net.minecraft.util.math.BlockPos

object PlacementUtils {
    val selectedPlacement: SchematicPlacement?
        get() = DataManager.getSchematicPlacementManager().selectedSchematicPlacement

    fun getWorldPos(local: BlockPos, region: String, placement: SchematicPlacement): BlockPos =
        placement.origin.add(
            PositionUtils.getTransformedPlacementPosition(
                local,
                placement,
                placement.getRelativeSubRegionPlacement(region)
            )
        )

    fun getPlacementPos(world: BlockPos): PlacementPos? {
        val parts = DataManager.getSchematicPlacementManager().getPlacementsTouching(world)
        for (part in parts) {
            if (!part.box.containsPos(world)) continue

            val placement = part.placement
            val region = part.subRegionName
            val local = SchematicUtils.getSchematicContainerPositionFromWorldPosition(
                world,
                placement.schematic,
                region,
                placement,
                placement.getRelativeSubRegionPlacement(region),
                placement.schematic.getSubRegionContainer(region)
            ) ?: continue
            return PlacementPos(world, local, region, placement)
        }
        return null
    }

    data class PlacementPos(val local: BlockPos, val region: String, val placement: SchematicPlacement) {
        val world: BlockPos
            get() {
                if (_world == null) _world = getWorldPos(local, region, placement)
                return _world!!
            }
        private var _world: BlockPos? = null

        constructor(world: BlockPos, local: BlockPos, region: String, placement: SchematicPlacement) : this(
            local,
            region,
            placement
        ) {
            this._world = world
        }

        val schematic
            get() = placement.schematic!!
        val state = schematic.getSubRegionContainer(region)!![local.x, local.y, local.z]
        val inventory = getBlockEntity() as? Inventory

        fun getBlockEntity(): BlockEntity? {
            val bes = schematic.getBlockEntityMapForRegion(region) ?: return null
            val provider = WorldUtils.getProvider(state) ?: return null
            val nbt = bes[local] ?: return null
            val be = provider.createBlockEntity(world, state) ?: return null
            be.readNbt(nbt)
            return be
        }
    }
}