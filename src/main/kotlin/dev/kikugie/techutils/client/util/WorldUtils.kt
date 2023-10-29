package dev.kikugie.techutils.client.util

import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemConvertible

object WorldUtils {
    fun getProvider(block: Block): BlockEntityProvider? = block as? BlockEntityProvider
    fun getProvider(state: BlockState): BlockEntityProvider? = getProvider(state.block)
    fun getProvider(item: ItemConvertible): BlockEntityProvider? =
        if (item.asItem() is BlockItem) getProvider((item as BlockItem).block) else null
}