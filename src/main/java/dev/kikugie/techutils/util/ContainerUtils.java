package dev.kikugie.techutils.util;

import fi.dy.masa.malilib.util.Constants;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContainerUtils {
	public static Optional<Inventory> validateContainer(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof BlockEntityProvider provider) {
			BlockEntity blockEntity;
			if (world != null && EntitiesDataStorage.getInstance().getBlockInventory(world, pos, true) instanceof BlockEntity be) {
				blockEntity = be;
			} else {
				blockEntity = provider.createBlockEntity(pos, state);
			}
			if (blockEntity instanceof Inventory inventory)
				return Optional.of(inventory);
		}
		return Optional.empty();
	}

	public static Optional<Inventory> validateContainer(BlockPos pos, BlockState state) {
		return validateContainer(null, pos, state);
	}

	public static boolean isChestAccessible(WorldAccess world, BlockPos pos, BlockState state) {
		assert state.getBlock() instanceof ChestBlock;
		if (ChestBlock.isChestBlocked(world, pos))
			return false;

		ChestType type = state.get(ChestBlock.CHEST_TYPE);
		if (type == ChestType.SINGLE)
			return true;

		BlockPos adjacent = pos.add(ChestBlock.getFacing(state).getVector());
		return world.getBlockState(adjacent).getBlock() != state.getBlock()
			|| !ChestBlock.isChestBlocked(world, adjacent);
	}

	public static boolean isShulkerBoxAccessible(WorldAccess world, BlockPos pos, BlockState state) {
		assert state.getBlock() instanceof ShulkerBoxBlock;
		ShulkerBoxBlockEntity box = (ShulkerBoxBlockEntity) world.getBlockEntity(pos);
		if (box == null || box.getAnimationStage() != ShulkerBoxBlockEntity.AnimationStage.CLOSED)
			return true;

		return world.isSpaceEmpty(ShulkerEntity
			.calculateBoundingBox(state.get(ShulkerBoxBlock.FACING), 0.0F, 0.5F)
			.offset(pos).contract(1.0E-6D));
	}

	public static List<Text> prettifyNbt(NbtElement nbt) {
		var style = Style.EMPTY.withColor(Formatting.WHITE).withItalic(false);
		var lines = new ArrayList<Text>();

		var currentLine = Text.empty().setStyle(style);
		lines.add(currentLine);
		Text prettyPrintedText = new NbtTextFormatter("    ", 0).apply(nbt);
		for (Text sibling : prettyPrintedText.getWithStyle(prettyPrintedText.getStyle())) {
			String string = sibling.getString();
			if (string.contains("\n")) {
				var parts = string.split("\n", 2);

				if (!parts[0].isEmpty())
					currentLine.append(Text.literal(parts[0]).setStyle(sibling.getStyle()));

				currentLine = Text.empty().setStyle(style);
				lines.add(currentLine);

				if (!parts[1].isEmpty())
					currentLine.append(Text.literal(parts[1]).setStyle(sibling.getStyle()));
			} else {
				currentLine.append(sibling);
			}
		}

		return lines;
	}

	/**
	 * (cloned from malilib)<br>
	 * Returns Inventory of items currently stored in the given NBT Items[] interface.
	 * Preserves empty slots, unless the "Inventory" interface is used.
	 *
	 * @param nbt       The tag holding the inventory contents
	 * @param slotCount the maximum number of slots, and thus also the size of the list to create
	 * @return Inventory
	 */
	public static Inventory getNbtInventory(@Nonnull NbtCompound nbt, int slotCount)
	{
		if (slotCount > 256)
		{
			slotCount = 256;
		}

		if (nbt.contains("Items"))
		{
			// Standard 'Items' tag for most Block Entities --
			// -- Furnace, Brewing Stand, Shulker Box, Crafter, Barrel, Chest, Dispenser, Hopper, Bookshelf, Campfire
			if (slotCount < 0)
			{
				NbtList list = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
				slotCount = list.size();
			}

			SimpleInventory inv = new SimpleInventory(slotCount);
			DefaultedList<ItemStack> items = DefaultedList.ofSize(slotCount, ItemStack.EMPTY);
			Inventories.readNbt(nbt, items);

			if (items.isEmpty())
			{
				return null;
			}

			for (int i = 0; i < slotCount; i++)
			{
				inv.setStack(i, items.get(i).copy());
			}

			return inv;
		}
		else if (nbt.contains("Inventory"))
		{
			// Entities use this (Piglin, Villager, a few others)
			if (slotCount < 0)
			{
				NbtList list = nbt.getList("Inventory", Constants.NBT.TAG_COMPOUND);
				slotCount = list.size();
			}

			SimpleInventory inv = new SimpleInventory(slotCount);
			inv.readNbtList(nbt.getList("Inventory", Constants.NBT.TAG_COMPOUND));

			if (inv.isEmpty())
			{
				return null;
			}

			return inv;
		}
		else if (nbt.contains("EnderItems"))
		{
			// Ender Chest
			if (slotCount < 0)
			{
				NbtList list = nbt.getList("EnderItems", Constants.NBT.TAG_COMPOUND);
				slotCount = list.size();
			}

			SimpleInventory inv = new SimpleInventory(slotCount);
			inv.readNbtList(nbt.getList("EnderItems", Constants.NBT.TAG_COMPOUND));

			if (inv.isEmpty())
			{
				return null;
			}

			return inv;
		}
		else if (nbt.contains("item"))
		{
			// item (DecoratedPot, ItemEntity)
			ItemStack entry = ItemStack.fromNbt(nbt.getCompound("item"));
			SimpleInventory inv = new SimpleInventory(1);
			inv.setStack(0, entry.copy());

			return inv;
		}
		else if (nbt.contains("Item"))
		{
			// Item (Item Frame)
			ItemStack entry = ItemStack.fromNbt(nbt.getCompound("Item"));
			SimpleInventory inv = new SimpleInventory(1);
			inv.setStack(0, entry.copy());

			return inv;
		}
		else if (nbt.contains("Book"))
		{
			// Book (Lectern)
			ItemStack entry = ItemStack.fromNbt(nbt.getCompound("Book"));
			SimpleInventory inv = new SimpleInventory(1);
			inv.setStack(0, entry.copy());

			return inv;
		}
		else if (nbt.contains("RecordItem"))
		{
			// RecordItem (Jukebox)
			ItemStack entry = ItemStack.fromNbt(nbt.getCompound("RecordItem"));
			SimpleInventory inv = new SimpleInventory(1);
			inv.setStack(0, entry.copy());

			return inv;
		}

		return null;
	}
}
