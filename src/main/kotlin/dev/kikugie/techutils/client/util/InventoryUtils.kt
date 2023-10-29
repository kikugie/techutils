package dev.kikugie.techutils.client.util

import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.block.enums.ChestType
import net.minecraft.entity.mob.ShulkerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldAccess


object InventoryUtils {
    fun isChestAccessible(world: WorldAccess, pos: BlockPos, state: BlockState): Boolean {
        require(state.block is ChestBlock)
        if (ChestBlock.isChestBlocked(world, pos)) return false
        val type = state.get(ChestBlock.CHEST_TYPE)
        if (type == ChestType.SINGLE) return true
        val adjacent = pos.add(ChestBlock.getFacing(state).vector)
        return (world.getBlockState(adjacent).block != state.block
                || !ChestBlock.isChestBlocked(world, adjacent))
    }

    fun isShulkerBoxAccessible(world: WorldAccess, pos: BlockPos?, state: BlockState): Boolean {
        require(state.block is ShulkerBoxBlock)
        val box = world.getBlockEntity(pos) as ShulkerBoxBlockEntity?
        return if (box == null || box.animationStage != ShulkerBoxBlockEntity.AnimationStage.CLOSED) false else world.isSpaceEmpty(
            ShulkerEntity
                .calculateBoundingBox(state.get(ShulkerBoxBlock.FACING), 0.0f, 0.5f)
                .offset(pos).contract(1.0E-6)
        )
    }
}