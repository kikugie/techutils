package dev.kikugie.techutils2.impl.structure.world

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

interface WorldWriter {
    fun setBlockState(pos: BlockPos, state: BlockState)
    fun setBlockEntity(pos: BlockPos, entity: BlockEntity)
    fun setBlockEntityData(pos: BlockPos, nbt: NbtCompound)
    fun addEntity(entity: Entity)

    operator fun set(pos: BlockPos, state: BlockState) = setBlockState(pos, state)
    operator fun set(pos: BlockPos, entity: BlockEntity) = setBlockEntity(pos, entity)
    operator fun set(pos: BlockPos, nbt: NbtCompound) = setBlockEntityData(pos, nbt)
}