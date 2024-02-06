package dev.kikugie.techutils.impl.structure.load

import dev.kikugie.techutils.impl.structure.world.WorldWriter
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.LitematicaSchematic.EntityInfo
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement
import fi.dy.masa.litematica.util.EntityUtils
import fi.dy.masa.litematica.util.PositionUtils
import net.minecraft.block.BlockEntityProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import kotlin.math.abs

fun LitematicaSchematic.placeTo(world: WorldWriter) {
    val placement = SchematicPlacement.createTemporary(this, BlockPos.ORIGIN)
    val subregions = placement.enabledRelativeSubRegionPlacements

    subregions.forEach { (name, region) ->
        writeData(
            world,
            region,
            getAreaSize(name)!!,
            getSubRegionContainer(name),
            getBlockEntityMapForRegion(name),
            getEntityListForRegion(name)
        )
    }
}

/**
 * Simplifies pasting process, given that the litematic is at the world origin and has no modified subregions.
 */
private fun writeData(
    world: WorldWriter,
    region: SubRegionPlacement,
    size: Vec3i,
    container: LitematicaBlockStateContainer?,
    blockEntities: Map<BlockPos, NbtCompound>?,
    entities: List<EntityInfo>?
) {
    val regionPos = region.pos
    val minCorner = PositionUtils.getMinCorner(
        BlockPos.ORIGIN,
        PositionUtils.getRelativeEndPositionFromAreaSize(size).add(regionPos)
    )
    val absSize = BlockPos(abs(size.x) - 1, abs(size.y) - 1, abs(size.z) - 1)

    if (container != null) for (localPos in BlockPos.iterate(BlockPos.ORIGIN, absSize)) {
        val pos = minCorner.add(localPos)
        val state = container[localPos.x, localPos.y, localPos.z]
        world[pos] = state

        val block = state.block
        if (block !is BlockEntityProvider) continue
        val nbt = blockEntities?.get(localPos)?.copy() ?: continue
        val blockEntity = block.createBlockEntity(pos, state) ?: continue
        with(nbt) {
            putInt("x", pos.x)
            putInt("y", pos.y)
            putInt("z", pos.z)
        }
        blockEntity.readNbt(nbt)
        world[pos] = blockEntity
    }

    if (entities != null) for (info in entities) {
        val entity = EntityUtils.createEntityAndPassengersFromNBT(
            info.nbt,
            MinecraftClient.getInstance().world
        ) ?: continue
        val entityPos = info.posVec.add(minCorner.x.toDouble(), minCorner.y.toDouble(), minCorner.z.toDouble())
        entity.setPosition(entityPos)
        spawnEntities(entity, world)
    }
}

private fun spawnEntities(entity: Entity, world: WorldWriter) {
    world.addEntity(entity)
    entity.passengerList.forEach {
        /*? if <1.20.2 */
        it.setPosition(entity.x, entity.y + entity.mountedHeightOffset + it.heightOffset, entity.z)
        /*? if >=1.20.2 */
        /*it.setPosition(entity.x, entity.y + entity.method_52536(it), entity.z)*/
        spawnEntities(it, world)
    }
}