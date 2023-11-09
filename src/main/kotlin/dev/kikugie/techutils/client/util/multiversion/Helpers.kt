package dev.kikugie.techutils.client.util.multiversion

import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager.PlacementPart
import fi.dy.masa.litematica.world.ChunkSchematic
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

/**
 * ItemStack.copyWithCount() is not available in 1.18
 */
fun ItemStack.withCount(count: Int): ItemStack = this.withCount(count)

/**
 * In 1.19 this constructor was replaced with a factory method
 */
fun Vec3d.floor(): BlockPos = BlockPos.ofFloored(this)


/**
 * In 1.18- Litematica stores entities per-subchunk
 */
fun ChunkSchematic.entities(): List<Entity> = this.entityList

/**
 * Still working in subchunks
 */
fun SchematicPlacementManager.getPlacementsTouching(pos: BlockPos): List<PlacementPart> = this.getAllPlacementsTouchingChunk(pos)