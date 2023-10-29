package dev.kikugie.techutils.test

import dev.kikugie.techutils.client.feature.misc.impl.GiveFullIInv.getStack
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.block.Blocks
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class GiveFullInvTest {
    /**
     * <pre>
     * Main hand: 1 diamond
     * Off hand: none
     * Expected result: 1 shulker box (1728 diamonds)</pre>
     */
    @Test
    fun itemInBox() {
        val testItem = testItem
        val fullBox: ItemStack? = getStack(testItem, ItemStack.EMPTY)
        Assertions.assertTrue(fullBox != null, "Failed to insert item in a box")
        boxOf(testItem.copyWithCount(64), fullBox!!, null)
    }

    /**
     * <pre>
     * Main hand: 1 diamond
     * Off hand: 1 white shulker box
     * Expected result: 1 white shulker box (1728 diamonds)</pre>
     */
    @Test
    fun itemInColoredBox() {
        val testItem = testItem
        val fullBox: ItemStack? = getStack(testItem, getEmptyBox(DyeColor.WHITE))
        Assertions.assertTrue(fullBox != null, "Failed to insert item in a box")
        boxOf(testItem.copyWithCount(64), fullBox!!, DyeColor.WHITE)
    }

    /**
     * <pre>
     * Main hand: 1 diamond
     * Off hand: 1 chest
     * Expected result: 1 chest (1728 diamonds)</pre>
     */
    @Test
    fun itemInChest() {
        val testItem = testItem
        val fullChest: ItemStack? = getStack(testItem, emptyChest)
        Assertions.assertTrue(fullChest != null, "Failed to insert item in a chest")
        chestOf(testItem.copyWithCount(64), fullChest!!)
    }

    /**
     * <pre>
     * Main hand: 1 diamond
     * Off hand: 1 bundle
     * Expected result: 1 bundle (64 *  diamonds)</pre>
     */
    @Test
    fun itemInBundle() {
        val testItem = testItem
        val fullBundle: ItemStack? = getStack(testItem, emptyBundle)
        Assertions.assertTrue(fullBundle != null, "Failed to insert item in a bundle")
        bundleOf(testItem.copyWithCount(64), fullBundle!!)
    }

    /**
     * <pre>
     * Main hand: 1 shulker box (1728 diamonds)
     * Off hand: none
     * Expected result: 1 chest (27 shulker boxes)</pre>
     */
    @Test
    fun boxInChest() {
        val testItem = testItem
        val fullBox: ItemStack? = getStack(testItem, ItemStack.EMPTY)
        Assertions.assertTrue(fullBox != null, "Failed to insert item in a box")
        val fullChest: ItemStack? = getStack(fullBox!!, emptyChest)
        Assertions.assertTrue(fullChest != null, "Failed to insert boxes in a chest")
        chestOf(fullBox, fullChest!!)
    }

    /**
     * <pre>
     * Main hand: 1 shulker box (1728 diamonds)
     * Off hand: 1 bundle
     * Expected result: 1 bundle (64 * BUNDLE_FILL shulker boxes)</pre>
     */
    @Test
    fun boxInBundle() {
        val testItem = testItem
        val fullBox: ItemStack? = getStack(testItem, ItemStack.EMPTY)
        Assertions.assertTrue(fullBox != null, "Failed to insert item in a box")
        val fullBundle: ItemStack? = getStack(fullBox!!, emptyBundle)
        Assertions.assertTrue(fullBundle != null, "Failed to insert boxes in a chest")
        bundleOf(fullBox, fullBundle!!)
    }

    /**
     * <pre>
     * Main hand: 1 shulker box (1728 diamonds)
     * Off hand: 1 shulker box
     * Expected result: none</pre>
     */
    @Test
    fun boxInBox() {
        val testItem = testItem
        val fullBox: ItemStack? = getStack(testItem, ItemStack.EMPTY)
        Assertions.assertTrue(fullBox != null, "Failed to insert item in a box")
        val fullBox2: ItemStack? = getStack(fullBox!!, getEmptyBox(null))
        Assertions.assertFalse(fullBox2 != null, "Should return no item")
    }

    /**
     * <pre>
     * Main hand: 1 white shulker box
     * Off hand: 1 chest
     * Expected result: 1 chest (1728 shulker boxes)</pre>
     */
    @Test
    fun emptyBoxInChest() {
        val testItem = getEmptyBox(DyeColor.WHITE)
        val fullChest: ItemStack? = getStack(testItem, emptyChest)
        Assertions.assertTrue(fullChest != null, "Failed to insert boxes in a chest")
        chestOf(testItem.copyWithCount(64), fullChest!!)
    }

    /**
     * <pre>
     * Main hand: 1 white shulker box
     * Off hand: none
     * Expected result: 1 chest (1728 shulker boxes)</pre>
     */
    @Test
    fun emptyBox() {
        val testItem = getEmptyBox(DyeColor.WHITE)
        val fullChest: ItemStack? = getStack(testItem, ItemStack.EMPTY)
        Assertions.assertTrue(fullChest != null, "Failed to insert boxes in a chest")
        chestOf(testItem.copyWithCount(64), fullChest!!)
    }

    /**
     * <pre>
     * Main hand: 1 white shulker box
     * Off hand: 1 bundle
     * Expected result: 1 bundle (64 * BUNDLE_FILL shulker boxes)</pre>
     */
    @Test
    fun emptyBoxInBundle() {
        val testItem = getEmptyBox(DyeColor.WHITE)
        val fullBundle: ItemStack? = getStack(testItem, emptyBundle)
        Assertions.assertTrue(fullBundle != null, "Failed to insert boxes in a bundle")
        bundleOf(testItem.copyWithCount(64), fullBundle!!)
    }

    /**
     * <pre>
     * Main hand: 1 white shulker box
     * Off hand: 1 shulker box
     * Expected result: none</pre>
     */
    @Test
    fun emptyBoxInBox() {
        val testItem = getEmptyBox(DyeColor.WHITE)
        val fullBox: ItemStack? = getStack(testItem, getEmptyBox(null))
        Assertions.assertFalse(fullBox != null, "Should return no item")
    }

    /**
     * <pre>
     * Main hand: none
     * Off hand: none
     * Expected result: none</pre>
     */
    @Test
    fun noItem() {
        val fullBox: ItemStack? = getStack(ItemStack.EMPTY, ItemStack.EMPTY)
        Assertions.assertFalse(fullBox != null, "Should return no item")
    }

    /**
     * <pre>
     * Main hand: none
     * Off hand: 1 shulker box
     * Expected result: none</pre>
     */
    @Test
    fun noItemWithOffhand() {
        val fullBox: ItemStack? = getStack(ItemStack.EMPTY, getEmptyBox(null))
        Assertions.assertFalse(fullBox != null, "Should return no item")
    }

    /**
     * <pre>
     * Main hand: 1 chest (1728 diamonds)
     * Off hand: 1 shulker box
     * Expected result: none</pre>
     */
    @Test
    fun chestInBoxInOffhand() {
        val testItem = testItem
        val fullChest: ItemStack? = getStack(testItem, emptyChest)
        Assertions.assertTrue(fullChest != null, "Failed to insert item in a box")
        val fullBox: ItemStack? = getStack(fullChest!!, getEmptyBox(null))
        Assertions.assertFalse(fullBox != null, "Should return no item")
    }

    /**
     * <pre>
     * Main hand: 1 chest (1728 diamonds)
     * Off hand: none
     * Expected result: none</pre>
     */
    @Test
    fun chestInBox() {
        val testItem = testItem
        val fullChest: ItemStack? = getStack(testItem, emptyChest)
        Assertions.assertTrue(fullChest != null, "Failed to insert item in a box")
        val fullBox: ItemStack? = getStack(fullChest!!, ItemStack.EMPTY)
        Assertions.assertFalse(fullBox != null, "Should return no item")
    }

    /**
     * <pre>
     * Main hand: 1 chest (1728 diamonds)
     * Off hand: 1 bundle
     * Expected result: none</pre>
     */
    @Test
    fun chestInBundle() {
        val testItem = testItem
        val fullChest: ItemStack? = getStack(testItem, emptyChest)
        Assertions.assertTrue(fullChest != null, "Failed to insert item in a box")
        val fullBundle: ItemStack? = getStack(fullChest!!, emptyBundle)
        Assertions.assertFalse(fullBundle != null, "Should return no item")
    }

    private fun boxOf(stack: ItemStack, box: ItemStack, color: DyeColor?) {
        Assertions.assertEquals(
            color,
            ShulkerBoxBlock.getColor(box.item),
            "Shulker box color '${ShulkerBoxBlock.getColor(box.item)}' doesn't match expected '$color'"
        )
        Assertions.assertTrue(box.hasNbt(), "Shulker box has no NBT")
        Assertions.assertTrue(
            box.nbt!!.contains("BlockEntityTag", NbtElement.COMPOUND_TYPE.toInt()),
            "Shulker box has no BlockEntityTag"
        )
        val shulker = ShulkerBoxBlockEntity.createFromNbt(
            BlockPos.ORIGIN,
            ShulkerBoxBlock.get(color).defaultState,
            box.getSubNbt("BlockEntityTag")
        ) as ShulkerBoxBlockEntity?
        for (i in 0 until shulker!!.size()) {
            Assertions.assertTrue(
                ItemStack.areEqual(stack, shulker.getStack(i)),
                "Shulker box item '${shulker.getStack(i)}' doesn't match expected '$stack'"
            )
        }
    }

    private fun chestOf(stack: ItemStack, chest: ItemStack) {
        Assertions.assertTrue(chest.hasNbt(), "Chest has no NBT")
        Assertions.assertTrue(
            chest.nbt!!.contains("BlockEntityTag", NbtElement.COMPOUND_TYPE.toInt()),
            "Chest has no BlockEntityTag"
        )
        val container = ChestBlockEntity.createFromNbt(
            BlockPos.ORIGIN,
            Blocks.CHEST.defaultState,
            chest.getSubNbt("BlockEntityTag")
        ) as ChestBlockEntity?
        for (i in 0 until container!!.size()) {
            Assertions.assertTrue(
                ItemStack.areEqual(stack, container.getStack(i)),
                "Chest item '${container.getStack(i)}' doesn't match expected '$stack'"
            )
        }
    }

    private fun bundleOf(stack: ItemStack, bundle: ItemStack) {
        Assertions.assertTrue(bundle.hasNbt(), "Bundle has no NBT")
        val nbtCompound = bundle.getOrCreateNbt()
        Assertions.assertTrue(nbtCompound.contains("Items", NbtElement.LIST_TYPE.toInt()), "Bundle has no Items tag")
        val items = nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE.toInt())
        Assertions.assertFalse(items.isEmpty(), "Bundle is empty")
        for (element in items) {
            val item = ItemStack.fromNbt(element as NbtCompound)
            Assertions.assertTrue(
                ItemStack.areEqual(stack, item),
                "Bundle item '$item' doesn't match expected '$stack'"
            )
        }
    }

    private val testItem: ItemStack
        get() = Items.DIAMOND.defaultStack

    private fun getEmptyBox(color: DyeColor?): ItemStack {
        return ShulkerBoxBlock.getItemStack(color)
    }

    private val emptyChest: ItemStack
        get() = Items.CHEST.defaultStack
    private val emptyDropper: ItemStack
        get() = Items.DROPPER.defaultStack
    private val emptyBundle: ItemStack
        get() = Items.BUNDLE.defaultStack

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            SharedConstants.createGameVersion()
            Bootstrap.initialize()
        }
    }
}
