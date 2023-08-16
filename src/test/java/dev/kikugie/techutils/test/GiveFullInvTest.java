package dev.kikugie.techutils.test;

import dev.kikugie.techutils.feature.GiveFullIInv;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import dev.kikugie.techutils.config.MiscConfigs;

public class GiveFullInvTest {
    @BeforeAll
    public static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    /**
     * <pre>
     *     Main hand: 1 diamond
     *     Off hand: none
     *     Expected result: 1 shulker box (1728 diamonds)</pre>
     */
    @Test
    public void itemInBox() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullBox = GiveFullIInv.get(testItem, ItemStack.EMPTY);
        Assertions.assertTrue(fullBox.isPresent(), "Failed to insert item in a box");
        boxOf(testItem.copyWithCount(64), fullBox.get(), null);
    }

    /**
     * <pre>
     *     Main hand: 1 diamond
     *     Off hand: 1 white shulker box
     *     Expected result: 1 white shulker box (1728 diamonds)</pre>
     */
    @Test
    public void itemInColoredBox() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullBox = GiveFullIInv.get(testItem, getEmptyBox(DyeColor.WHITE));
        Assertions.assertTrue(fullBox.isPresent(), "Failed to insert item in a box");
        boxOf(testItem.copyWithCount(64), fullBox.get(), DyeColor.WHITE);
    }

    /**
     * <pre>
     *     Main hand: 1 diamond
     *     Off hand: 1 chest
     *     Expected result: 1 chest (1728 diamonds)</pre>
     */
    @Test
    public void itemInChest() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullChest = GiveFullIInv.get(testItem, getEmptyChest());
        Assertions.assertTrue(fullChest.isPresent(), "Failed to insert item in a chest");
        chestOf(testItem.copyWithCount(64), fullChest.get());
    }

    /**
     * <pre>
     *     Main hand: 1 diamond
     *     Off hand: 1 bundle
     *     Expected result: 1 bundle (64 * {@link MiscConfigs#BUNDLE_FILL} diamonds)</pre>
     */
    @Test
    public void itemInBundle() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullBundle = GiveFullIInv.get(testItem, getEmptyBundle());
        Assertions.assertTrue(fullBundle.isPresent(), "Failed to insert item in a bundle");
        bundleOf(testItem.copyWithCount(64), fullBundle.get());
    }

    /**
     * <pre>
     *     Main hand: 1 shulker box (1728 diamonds)
     *     Off hand: none
     *     Expected result: 1 chest (27 shulker boxes)</pre>
     */
    @Test
    public void boxInChest() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullBox = GiveFullIInv.get(testItem, ItemStack.EMPTY);
        Assertions.assertTrue(fullBox.isPresent(), "Failed to insert item in a box");

        Optional<ItemStack> fullChest = GiveFullIInv.get(fullBox.get(), getEmptyChest());
        Assertions.assertTrue(fullChest.isPresent(), "Failed to insert boxes in a chest");
        chestOf(fullBox.get(), fullChest.get());
    }

    /**
     * <pre>
     *     Main hand: 1 shulker box (1728 diamonds)
     *     Off hand: 1 bundle
     *     Expected result: 1 bundle (64 * {@link MiscConfigs#BUNDLE_FILL} shulker boxes)</pre>
     */
    @Test
    public void boxInBundle() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullBox = GiveFullIInv.get(testItem, ItemStack.EMPTY);
        Assertions.assertTrue(fullBox.isPresent(), "Failed to insert item in a box");

        Optional<ItemStack> fullBundle = GiveFullIInv.get(fullBox.get(), getEmptyBundle());
        Assertions.assertTrue(fullBundle.isPresent(), "Failed to insert boxes in a chest");
        bundleOf(fullBox.get(), fullBundle.get());
    }

    /**
     * <pre>
     *     Main hand: 1 shulker box (1728 diamonds)
     *     Off hand: 1 shulker box
     *     Expected result: none</pre>
     */
    @Test
    public void boxInBox() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullBox = GiveFullIInv.get(testItem, ItemStack.EMPTY);
        Assertions.assertTrue(fullBox.isPresent(), "Failed to insert item in a box");

        Optional<ItemStack> fullBox2 = GiveFullIInv.get(fullBox.get(), getEmptyBox(null));
        Assertions.assertFalse(fullBox2.isPresent(), "Should return no item");
    }

    /**
     * <pre>
     *     Main hand: 1 white shulker box
     *     Off hand: 1 chest
     *     Expected result: 1 chest (1728 shulker boxes)</pre>
     */
    @Test
    public void emptyBoxInChest() {
        ItemStack testItem = getEmptyBox(DyeColor.WHITE);
        Optional<ItemStack> fullChest = GiveFullIInv.get(testItem, getEmptyChest());
        Assertions.assertTrue(fullChest.isPresent(), "Failed to insert boxes in a chest");
        chestOf(testItem.copyWithCount(64), fullChest.get());
    }

    /**
     * <pre>
     *     Main hand: 1 white shulker box
     *     Off hand: none
     *     Expected result: 1 chest (1728 shulker boxes)</pre>
     */
    @Test
    public void emptyBox() {
        ItemStack testItem = getEmptyBox(DyeColor.WHITE);
        Optional<ItemStack> fullChest = GiveFullIInv.get(testItem, ItemStack.EMPTY);
        Assertions.assertTrue(fullChest.isPresent(), "Failed to insert boxes in a chest");
        chestOf(testItem.copyWithCount(64), fullChest.get());
    }

    /**
     * <pre>
     *     Main hand: 1 white shulker box
     *     Off hand: 1 bundle
     *     Expected result: 1 bundle (64 * {@link MiscConfigs#BUNDLE_FILL} shulker boxes)</pre>
     */
    @Test
    public void emptyBoxInBundle() {
        ItemStack testItem = getEmptyBox(DyeColor.WHITE);
        Optional<ItemStack> fullBundle = GiveFullIInv.get(testItem, getEmptyBundle());
        Assertions.assertTrue(fullBundle.isPresent(), "Failed to insert boxes in a bundle");
        bundleOf(testItem.copyWithCount(64), fullBundle.get());
    }

    /**
     * <pre>
     *     Main hand: 1 white shulker box
     *     Off hand: 1 shulker box
     *     Expected result: none</pre>
     */
    @Test
    public void emptyBoxInBox() {
        ItemStack testItem = getEmptyBox(DyeColor.WHITE);
        Optional<ItemStack> fullBox = GiveFullIInv.get(testItem, getEmptyBox(null));
        Assertions.assertFalse(fullBox.isPresent(), "Should return no item");
    }

    /**
     * <pre>
     *     Main hand: none
     *     Off hand: none
     *     Expected result: none</pre>
     */
    @Test
    public void noItem() {
        Optional<ItemStack> fullBox = GiveFullIInv.get(ItemStack.EMPTY, ItemStack.EMPTY);
        Assertions.assertFalse(fullBox.isPresent(), "Should return no item");
    }

    /**
     * <pre>
     *     Main hand: none
     *     Off hand: 1 shulker box
     *     Expected result: none</pre>
     */
    @Test
    public void noItemWithOffhand() {
        Optional<ItemStack> fullBox = GiveFullIInv.get(ItemStack.EMPTY, getEmptyBox(null));
        Assertions.assertFalse(fullBox.isPresent(), "Should return no item");
    }

    /**
     * <pre>
     *     Main hand: 1 chest (1728 diamonds)
     *     Off hand: 1 shulker box
     *     Expected result: none</pre>
     */
    @Test
    public void chestInBoxInOffhand() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullChest = GiveFullIInv.get(testItem, getEmptyChest());
        Assertions.assertTrue(fullChest.isPresent(), "Failed to insert item in a box");

        Optional<ItemStack> fullBox = GiveFullIInv.get(fullChest.get(), getEmptyBox(null));
        Assertions.assertFalse(fullBox.isPresent(), "Should return no item");
    }

    /**
     * <pre>
     *     Main hand: 1 chest (1728 diamonds)
     *     Off hand: none
     *     Expected result: none</pre>
     */
    @Test
    public void chestInBox() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullChest = GiveFullIInv.get(testItem, getEmptyChest());
        Assertions.assertTrue(fullChest.isPresent(), "Failed to insert item in a box");

        Optional<ItemStack> fullBox = GiveFullIInv.get(fullChest.get(), ItemStack.EMPTY);
        Assertions.assertFalse(fullBox.isPresent(), "Should return no item");
    }

    /**
     * <pre>
     *     Main hand: 1 chest (1728 diamonds)
     *     Off hand: 1 bundle
     *     Expected result: none</pre>
     */
    @Test
    public void chestInBundle() {
        ItemStack testItem = getTestItem();
        Optional<ItemStack> fullChest = GiveFullIInv.get(testItem, getEmptyChest());
        Assertions.assertTrue(fullChest.isPresent(), "Failed to insert item in a box");

        Optional<ItemStack> fullBundle = GiveFullIInv.get(fullChest.get(), getEmptyBundle());
        Assertions.assertFalse(fullBundle.isPresent(), "Should return no item");
    }

    @SuppressWarnings("DataFlowIssue")
    private void boxOf(ItemStack stack, ItemStack box, @Nullable DyeColor color) {
        Assertions.assertEquals(color, ShulkerBoxBlock.getColor(box.getItem()), "Shulker box color '%s' doesn't match expected '%s'".formatted(ShulkerBoxBlock.getColor(box.getItem()), color));
        Assertions.assertTrue(box.hasNbt(), "Shulker box has no NBT");
        Assertions.assertTrue(box.getNbt().contains("BlockEntityTag", NbtElement.COMPOUND_TYPE), "Shulker box has no BlockEntityTag");

        ShulkerBoxBlockEntity shulker = (ShulkerBoxBlockEntity) ShulkerBoxBlockEntity.createFromNbt(BlockPos.ORIGIN, ShulkerBoxBlock.get(color).getDefaultState(), box.getSubNbt("BlockEntityTag"));
        for (int i = 0; i < shulker.size(); i++) {
            Assertions.assertTrue(ItemStack.areEqual(stack, shulker.getStack(i)), "Shulker box item '%s' doesn't match expected '%s'".formatted(shulker.getStack(i), stack));
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void chestOf(ItemStack stack, ItemStack chest) {
        Assertions.assertTrue(chest.hasNbt(), "Chest has no NBT");
        Assertions.assertTrue(chest.getNbt().contains("BlockEntityTag", NbtElement.COMPOUND_TYPE), "Chest has no BlockEntityTag");

        ChestBlockEntity container = (ChestBlockEntity) ChestBlockEntity.createFromNbt(BlockPos.ORIGIN, Blocks.CHEST.getDefaultState(), chest.getSubNbt("BlockEntityTag"));
        for (int i = 0; i < container.size(); i++) {
            Assertions.assertTrue(ItemStack.areEqual(stack, container.getStack(i)), "Chest item '%s' doesn't match expected '%s'".formatted(container.getStack(i), stack));
        }
    }

    private void bundleOf(ItemStack stack, ItemStack bundle) {
        Assertions.assertTrue(bundle.hasNbt(), "Bundle has no NBT");
        NbtCompound nbtCompound = bundle.getOrCreateNbt();
        Assertions.assertTrue(nbtCompound.contains("Items", NbtElement.LIST_TYPE), "Bundle has no Items tag");

        NbtList items = nbtCompound.getList("Items", NbtElement.COMPOUND_TYPE);
        Assertions.assertFalse(items.isEmpty(), "Bundle is empty");

        for (NbtElement element : items) {
            ItemStack item = ItemStack.fromNbt((NbtCompound) element);
            Assertions.assertTrue(ItemStack.areEqual(stack, item), "Bundle item '%s' doesn't match expected '%s'".formatted(item, stack));
        }
    }

    private ItemStack getTestItem() {
        return Items.DIAMOND.getDefaultStack();
    }

    private ItemStack getEmptyBox(@Nullable DyeColor color) {
        return ShulkerBoxBlock.getItemStack(color);
    }

    private ItemStack getEmptyChest() {
        return Items.CHEST.getDefaultStack();
    }

    private ItemStack getEmptyDropper() {
        return Items.DROPPER.getDefaultStack();
    }

    private ItemStack getEmptyBundle() {
        return Items.BUNDLE.getDefaultStack();
    }
}
