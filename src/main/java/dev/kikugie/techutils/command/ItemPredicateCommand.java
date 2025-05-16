package dev.kikugie.techutils.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.kikugie.techutils.TechUtilsMod;
import dev.kikugie.techutils.feature.containerscan.verifier.ItemPredicateEntryScreen;
import dev.kikugie.techutils.util.ItemPredicateUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ItemPredicateCommand {
	private static final SimpleCommandExceptionType WRONG_MAIN_HAND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.itempredicate.placeholder.wrong_main_hand"));
	private static final SimpleCommandExceptionType NO_PLACEHOLDER_FOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.itempredicate.placeholder.get.not_found"));
	private static final SimpleCommandExceptionType NOT_IN_CREATIVE_MODE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.itempredicate.not_in_creative_mode"));

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess ignoredAccess) {
		dispatcher.register(literal("itempredicate")
			.then(literal("give")
				.executes(context -> {
					var player = context.getSource().getPlayer();

					enforceCreativeMode(player);

					var offHandStack = player.getOffHandStack().copy();
					TechUtilsMod.QUEUED_END_CLIENT_TICK_TASKS.add(client -> client.setScreen(new ItemPredicateEntryScreen(player, offHandStack)));
					return 1;
				})
			)
			.then(literal("edit")
				.executes(context -> {
					var source = context.getSource();
					var player = source.getPlayer();

					enforceCreativeMode(player);

					var mainHandStack = player.getMainHandStack().copy();

					if (!ItemPredicateUtils.isPredicate(mainHandStack)) {
						throw WRONG_MAIN_HAND_EXCEPTION.create();
					}

					var rawPredicate = ItemPredicateUtils.getRawPredicate(mainHandStack);
					var placeholder = ItemPredicateUtils.getPlaceholder(mainHandStack);

					TechUtilsMod.QUEUED_END_CLIENT_TICK_TASKS.add(client -> client.setScreen(new ItemPredicateEntryScreen(context.getSource().getPlayer(), rawPredicate, placeholder)));
					return 1;
				})
			)
			.then(literal("placeholder")
				.then(literal("set")
					.executes(context -> {
						var source = context.getSource();
						var player = source.getPlayer();

						enforceCreativeMode(player);

						var mainHandStack = player.getMainHandStack().copy();
						var offHandStack = player.getOffHandStack().copy();

						if (!ItemPredicateUtils.isPredicate(mainHandStack)) {
							throw WRONG_MAIN_HAND_EXCEPTION.create();
						}

						ItemPredicateUtils.setPlaceholder(mainHandStack, offHandStack);
						int selectedSlot = player.getInventory().getSelectedSlot();
						player.getInventory().setStack(selectedSlot, mainHandStack);
						source.getClient().interactionManager.clickCreativeStack(mainHandStack, 36 + selectedSlot);
						player.playerScreenHandler.sendContentUpdates();

						source.sendFeedback(Text.translatable("commands.itempredicate.placeholder.set.success"));

						return 1;
					})
				)
				.then(literal("get")
					.executes(context -> {
						var source = context.getSource();
						var player = source.getPlayer();

						enforceCreativeMode(player);

						var mainHandStack = player.getMainHandStack().copy();

						if (!ItemPredicateUtils.isPredicate(mainHandStack)) {
							throw WRONG_MAIN_HAND_EXCEPTION.create();
						}

						if (!(ItemPredicateUtils.getPlaceholder(mainHandStack) instanceof ItemStack placeholder)) {
							throw NO_PLACEHOLDER_FOUND_EXCEPTION.create();
						}

						player.getInventory().setStack(PlayerInventory.OFF_HAND_SLOT, placeholder);
						source.getClient().interactionManager.clickCreativeStack(placeholder, 45);
						player.playerScreenHandler.sendContentUpdates();

						source.sendFeedback(Text.translatable("commands.itempredicate.placeholder.get.success"));

						return 1;
					})
				)
			)
		);
	}

	private static void enforceCreativeMode(ClientPlayerEntity player) throws CommandSyntaxException {
		if (!player.isCreative())
			throw NOT_IN_CREATIVE_MODE_EXCEPTION.create();
	}
}
