package dev.kikugie.techutils.client.feature.containerscan.screen

import dev.kikugie.techutils.client.util.inventory.DefaultedInventory
import dev.kikugie.techutils.mixin.client.ScreenHandlerAccessor
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler


class CatchingScreenHandler(
    container: ScreenHandler,
    private val action: (DefaultedInventory) -> Unit
) : ScreenHandler((container as ScreenHandlerAccessor).type, container.syncId) {
    private val playerSlots: Set<Int>

    init {
        playerSlots = container.slots.filter { it.inventory is PlayerInventory }.map { it.id }.toSet()
    }

    override fun quickMove(player: PlayerEntity, slot: Int) = ItemStack.EMPTY

    override fun canUse(player: PlayerEntity) = true

    override fun updateSlotStacks(revision: Int, stacks: MutableList<ItemStack>, cursorStack: ItemStack?) {
        val inv = DefaultedInventory(ItemStack.EMPTY)
        stacks.forEachIndexed { i, stack -> if (i !in playerSlots) inv[i] = stack }
        action.run { inv }
    }
}