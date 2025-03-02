package dev.kikugie.techutils.util;

import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.malilib.render.InventoryOverlay;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.Component;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContainerUtils {
	public static Optional<Inventory> validateContainer(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof BlockEntityProvider provider) {
			BlockEntity blockEntity;
			if (world != null && InventoryUtils.getTargetInventory(world, pos) instanceof InventoryOverlay.Context ctx && ctx.be() instanceof BlockEntity be) {
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
			.calculateBoundingBox(1.0F, state.get(ShulkerBoxBlock.FACING), 0.0F, new Vec3d(0.5, 0.0, 0.5))
			.offset(pos).contract(1.0E-6D));
	}

	public static List<Text> getFormattedComponents(ItemStack stack) {
		var ops = RegistryOps.of(NbtOps.INSTANCE, MinecraftClient.getInstance().world.getRegistryManager());
		var lines = new ArrayList<Text>();
		for (Component<?> component : stack.getComponents()) {
			component.encode(ops).mapOrElse(
				nbt -> {
					var text = Text.empty();
					text.styled(style -> style.withColor(Formatting.WHITE).withItalic(false));
					text.append(Text.literal(String.valueOf(Registries.DATA_COMPONENT_TYPE.getId(component.type())))
						.styled(style -> style.withColor(Formatting.GRAY))
					);
					text.append(" => ");

					var prettyLines = prettifyNbt(nbt);
					text.append(prettyLines.getFirst());

					lines.add(text);
					lines.addAll(prettyLines.subList(1, prettyLines.size()));

					return null;
				},
				e -> {
					var error = Text.literal(
						"Failed to encode component '%s' - %s"
							.formatted(Registries.DATA_COMPONENT_TYPE.getId(component.type()), e.message())
					).styled(style -> style.withColor(Formatting.RED).withItalic(false));
					lines.add(error);
					return null;
				}
			);
		}
		return lines;
	}

	public static List<Text> prettifyNbt(NbtElement nbt) {
		var style = Style.EMPTY.withColor(Formatting.WHITE).withItalic(false);
		var lines = new ArrayList<Text>();

		var currentLine = Text.empty().setStyle(style);
		lines.add(currentLine);
		Text prettyPrintedText = new NbtTextFormatter("    ").apply(nbt);
		for (Text sibling : prettyPrintedText.getSiblings()) {
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
}
