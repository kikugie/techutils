package dev.kikugie.techutils.feature;

import dev.kikugie.techutils.TechUtilsMod;
import dev.kikugie.techutils.config.MiscConfigs;
import fi.dy.masa.malilib.util.game.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Inserts a full container of given item into player's hand. Works <b>only</b> in creative mode.
 */
public class GiveFullIInv {
	private static final GiveFullIInv INSTANCE = new GiveFullIInv();
	private static final Supplier<Boolean> SAFETY = MiscConfigs.FILL_SAFETY::getBooleanValue;

	public static boolean onKeybind() {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		assert player != null;
		if (!player.isCreative()) {
			INSTANCE.sendError("not_creative_enough");
			return false;
		}

		ItemStack mainHand = player.getMainHandStack();
		ItemStack offHand = player.getOffHandStack();

		Optional<ItemStack> result = get(mainHand, offHand);
		if (result.isEmpty())
			return false;
		int selectedSlot = player.getInventory().getSelectedSlot();
		player.getInventory().setStack(selectedSlot, result.get());
		Objects.requireNonNull(MinecraftClient.getInstance().interactionManager).clickCreativeStack(result.get(), 36 + selectedSlot);
		player.playerScreenHandler.sendContentUpdates();
		return true;
	}

	public static Optional<ItemStack> get(ItemStack mainHand, ItemStack offHand) {
		return INSTANCE.getItem(mainHand, offHand);
	}

	public static ItemStack fillShulker(ItemStack stack, @Nullable DyeColor color) {
		Block shulker = ShulkerBoxBlock.get(color);
		ShulkerBoxBlockEntity box = new ShulkerBoxBlockEntity(BlockPos.ORIGIN, shulker.getDefaultState());
		return fillLootable(stack, shulker.asItem(), box);
	}

	public static ItemStack fillChest(ItemStack stack) {
		Block chest = Blocks.CHEST;
		ChestBlockEntity box = new ChestBlockEntity(BlockPos.ORIGIN, chest.getDefaultState());
		return fillLootable(stack, chest.asItem(), box);
	}

	public static ItemStack fillLootable(ItemStack stack, Item item, LootableContainerBlockEntity lootable) {
		for (int i = 0; i < lootable.size(); i++) {
			lootable.setStack(i, stack);
		}
		ItemStack container = item.getDefaultStack();
		BlockUtils.setStackNbt(container, lootable, MinecraftClient.getInstance().world.getRegistryManager());
		return container;
	}

	public static ItemStack fillBundle(ItemStack stack) {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < MiscConfigs.BUNDLE_FILL.getIntegerValue(); i++) {
			stacks.add(stack.copy());
		}
		ItemStack bundle = Items.BUNDLE.getDefaultStack();
		BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(new BundleContentsComponent(stacks));
		bundle.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
		return bundle;
	}

	@SuppressWarnings("DataFlowIssue")
	public static boolean containerHasItems(ItemStack container) {
		if (container.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof BlockEntityProvider provider) {
			var container_data = container.get(DataComponentTypes.CONTAINER);
			if (container_data == null)
				return false;

			BlockEntity blockEntity = provider.createBlockEntity(BlockPos.ORIGIN, blockItem.getBlock().getDefaultState());
			blockEntity.readComponents(container);
			if (blockEntity instanceof Inventory inventory)
				return !inventory.isEmpty();
		}
		return false;
	}

	public static boolean bundleHasItems(ItemStack bundle) {
		return BundleItem.getAmountFilled(bundle) > 0;
	}

	private static String generateCommand(ItemStack stack, int slot) {
		String template = "item replace entity @s container.%s with %s 1";
		return "";
	}

	private Optional<ItemStack> getItem(ItemStack mainHand, ItemStack offHand) {
		if (mainHand.isEmpty()) {
			sendError("no_item");
			return Optional.empty();
		}

		return isShulkerBox(mainHand) ? handleBox(mainHand, offHand) : handleItem(mainHand, offHand);
	}

	private Optional<ItemStack> handleItem(ItemStack mainHand, ItemStack offHand) {
		if (!SAFETY.get() && !recursionCheck(mainHand)) {
			sendError("nested_stack");
			return Optional.empty();
		}
		ItemStack fullStack = mainHand.copyWithCount(mainHand.getMaxCount());
		return Optional.of(handleOffHand(offHand, stack -> fillShulker(stack, null)).apply(fullStack));
	}

	private Optional<ItemStack> handleBox(ItemStack mainHand, ItemStack offHand) {
		if (!SAFETY.get() && isShulkerBox(offHand)) {
			sendError("nested_box");
			return Optional.empty();
		}
		ItemStack fullStack = containerHasItems(mainHand) ? mainHand.copy() : mainHand.copyWithCount(64);
		return Optional.of(handleOffHand(offHand, GiveFullIInv::fillChest).apply(fullStack));
	}

	private boolean recursionCheck(ItemStack mainHand) {
		if (mainHand.getItem() instanceof BundleItem)
			return !bundleHasItems(mainHand);
		return !containerHasItems(mainHand);
	}

	private Function<ItemStack, ItemStack> handleOffHand(ItemStack offHand, @Nullable Function<ItemStack, ItemStack> fallback) {
		if (offHand.isEmpty())
			return fallback;
		// Item has a corresponding block entity
		if (offHand.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof BlockEntityProvider provider) {
			BlockEntity blockEntity = provider.createBlockEntity(BlockPos.ORIGIN, blockItem.getBlock().getDefaultState());
			// Block entity is a container
			if (blockEntity instanceof LootableContainerBlockEntity lootable)
				return stack -> fillLootable(stack, blockItem, lootable);
		}
		if (offHand.getItem() instanceof BundleItem) {
			return GiveFullIInv::fillBundle;
		}
		// Some other item
		return fallback;
	}

	private void sendError(String key) {
		Text message = Text.translatable("techutils.feature.givefullinv." + key).formatted(Formatting.DARK_RED);
		if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null)
			MinecraftClient.getInstance().player.sendMessage(message, true);
		else
			TechUtilsMod.LOGGER.warn(message.getString());
	}

	private boolean isShulkerBox(ItemStack stack) {
		return stack.getItem().toString().contains("shulker_box");
	}
}
