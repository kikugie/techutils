package dev.kikugie.techutils.client.util.inventory

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack

class DefaultedInventory(private val default: ItemStack, stacks: Collection<ItemStack> = emptyList()) : Inventory {
    private val contents: MutableList<ItemStack>

    init {
        contents = ArrayList(stacks)
    }

    override fun clear() = contents.clear()
    override fun size() = contents.size
    override fun isEmpty() = contents.isEmpty()
    override fun getStack(slot: Int) = if (isInBounds(slot)) contents[slot] else default
    override fun removeStack(slot: Int, amount: Int) =
        if (isInBounds(slot)) contents[slot].split(amount) else default

    override fun removeStack(slot: Int) = if (isInBounds(slot)) contents.removeAt(slot) else default
    override fun setStack(slot: Int, stack: ItemStack) {
        if (isInBounds(slot)) contents[slot] = stack
    }

    override fun markDirty() {
    }

    override fun canPlayerUse(player: PlayerEntity?) = true
    private fun isInBounds(slot: Int) = slot in 0 until size()
    operator fun get(slot: Int) = getStack(slot)

    operator fun set(slot: Int, stack: ItemStack) {
        contents[slot] = stack
    }

    operator fun iterator() = contents.iterator()
    operator fun contains(item: ItemConvertible): Boolean {
        val i = item.asItem()
        for (stack in contents)
            if (stack.isOf(i)) return true
        return false
    }
}