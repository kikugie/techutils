package dev.kikugie.techutils.client.util.multiversion

import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager.PlacementPart
import fi.dy.masa.litematica.world.ChunkSchematic
import fi.dy.masa.malilib.util.SubChunkPos
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

fun ItemStack.withCount(count: Int): ItemStack {
    val itemStack = this.copy()
    itemStack.count = count
    return itemStack
}

fun Vec3d.floor(): BlockPos = BlockPos(this)

fun ChunkSchematic.entities(): List<Entity> {
    val entities = ArrayList<Entity>()
    for (i in 0 until this.countVerticalSections())
        entities.addAll(this.getEntityListForSectionIfExists(i))
    return entities
}

fun SchematicPlacementManager.getPlacementsTouching(pos: BlockPos): List<PlacementPart> =
    this.getAllPlacementsTouchingSubChunk(SubChunkPos(pos))