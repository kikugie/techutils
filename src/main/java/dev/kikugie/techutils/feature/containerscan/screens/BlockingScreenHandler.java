package dev.kikugie.techutils.feature.containerscan.screens;

import dev.kikugie.techutils.mixin.containerscan.ScreenHandlerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

import java.util.List;

/**
 * Used to record {@link HandledScreen} contents into an inventory reference and close the screen.
 */
public class BlockingScreenHandler extends ScreenHandler {
	private final SimpleInventory inv;

	public BlockingScreenHandler(ScreenHandler container, SimpleInventory inventoryLink) {
		super(((ScreenHandlerAccessor) container).getNullableType(), container.syncId);
		this.inv = inventoryLink;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void updateSlotStacks(int revision, List<ItemStack> stacks, ItemStack cursorStack) {
		for (int i = 0; i < Math.min(this.inv.size(), stacks.size()); i++)
			this.inv.setStack(i, stacks.get(i));
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		assert player != null;
		player.closeHandledScreen();
	}
}
