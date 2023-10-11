package dev.kikugie.techutils.client.feature.giveinv

import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.Blocks
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.LootableContainerBlockEntity
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.item.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import java.util.function.Function

/**
 * Inserts a full container of given item into player's hand. Works **only** in creative mode.
 */
object GiveFullIInv {
    private val safety = { GiveFullInvConfig.fillSafety.booleanValue }
    private val bundleFill = { GiveFullInvConfig.bundleFill.integerValue }
    fun onKeybind(): Boolean {
        val client = MinecraftClient.getInstance()
        val player = client.player!!
        if (!player.isCreative) {
            sendError("not_creative_enough")
            return false
        }
        val mainHand = player.mainHandStack.copy()
        val offHand = player.offHandStack.copy()

        val result: ItemStack = getStack(mainHand, offHand) ?: return false
        client.interactionManager?.clickCreativeStack(result, 36 + player.inventory.selectedSlot)
        return true
    }

    fun getStack(mainHand: ItemStack, offHand: ItemStack): ItemStack? {
        if (mainHand.isEmpty) {
            sendError("no_item")
            return null
        }

        return if (isShulkerBox(mainHand))
            handleBox(mainHand, offHand)
        else handleItem(mainHand, offHand)
    }

    private fun handleBox(mainHand: ItemStack, offHand: ItemStack): ItemStack? {
        if (safety.invoke() && isShulkerBox(offHand)) {
            sendError("nested_box")
            return null
        }
        val fullStack = if (containerHasItems(mainHand))
            mainHand.copy()
        else
            mainHand.copyWithCount(64)
        return handleOffHand(offHand) { stack: ItemStack -> fillChest(stack) }.apply(fullStack)
    }

    private fun handleItem(mainHand: ItemStack, offHand: ItemStack): ItemStack? {
        if (safety.invoke() && !checkRecursion(mainHand)) {
            sendError("nested_stack")
            return null
        }
        val fullStack = mainHand.copyWithCount(mainHand.maxCount)
        return handleOffHand(offHand) { stack: ItemStack ->
            fillShulker(stack, null)
        }.apply(fullStack)
    }

    private fun handleOffHand(
        offHand: ItemStack,
        fallback: Function<ItemStack, ItemStack>
    ): Function<ItemStack, ItemStack> {
        if (offHand.isEmpty) return fallback
        // Item has a corresponding block entity
        if (offHand.item is BlockItem && (offHand.item as BlockItem).block is BlockEntityProvider) {
            val blockItem = offHand.item as BlockItem
            val provider = blockItem.block as BlockEntityProvider
            val blockEntity: BlockEntity? = provider.createBlockEntity(BlockPos.ORIGIN, blockItem.block.defaultState)
            // Block entity is a container
            if (blockEntity is LootableContainerBlockEntity)
                return Function<ItemStack, ItemStack> { stack: ItemStack ->
                    fillLootable(
                        stack,
                        blockItem,
                        blockEntity
                    )
                }
        }
        return if (offHand.item is BundleItem) {
            Function<ItemStack, ItemStack> { stack: ItemStack -> fillBundle(stack) }
        } else fallback
    }

    private fun fillShulker(stack: ItemStack, color: DyeColor?): ItemStack {
        val shulker = ShulkerBoxBlock.get(color)
        val box = ShulkerBoxBlockEntity(BlockPos.ORIGIN, shulker.defaultState)
        return fillLootable(stack, shulker.asItem(), box)
    }

    private fun fillChest(stack: ItemStack): ItemStack {
        val chest = Blocks.CHEST
        val box = ChestBlockEntity(BlockPos.ORIGIN, chest.defaultState)
        return fillLootable(stack, chest.asItem(), box)
    }

    private fun fillLootable(stack: ItemStack, item: Item, lootable: LootableContainerBlockEntity): ItemStack {
        for (i in 0 until lootable.size()) {
            lootable.setStack(i, stack)
        }
        val container = item.defaultStack
        lootable.setStackNbt(container)
        return container
    }

    private fun fillBundle(stack: ItemStack): ItemStack {
        val bundle = Items.BUNDLE.defaultStack
        val nbtCompound = bundle.getOrCreateNbt()
        if (!nbtCompound.contains("Items")) {
            nbtCompound.put("Items", NbtList())
        }
        val nbtList = nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE.toInt())
        val nbt = NbtCompound()
        stack.writeNbt(nbt)
        for (i in 0 until bundleFill.invoke()) {
            nbtList.add(nbt.copy())
        }
        return bundle
    }

    private fun checkRecursion(mainHand: ItemStack): Boolean {
        if (!mainHand.hasNbt()) return true
        return if (mainHand.item is BundleItem)
            !bundleHasItems(mainHand)
        else !containerHasItems(mainHand)
    }

    private fun isShulkerBox(stack: ItemStack): Boolean {
        return stack.item.toString().contains("shulker_box")
    }

    private fun containerHasItems(container: ItemStack): Boolean {
        if (container.item !is BlockItem || (container.item as BlockItem).block !is BlockEntityProvider)
            return false
        return container.getOrCreateSubNbt("BlockEntityTag").contains("Items")
    }

    private fun bundleHasItems(bundle: ItemStack): Boolean {
        val nbtCompound = bundle.getOrCreateNbt()
        return if (!nbtCompound.contains("Items"))
            false
        else !nbtCompound.getList(
            "Items",
            NbtElement.COMPOUND_TYPE.toInt()
        ).isEmpty()
    }

    private fun sendError(error: String) {
    }
}
