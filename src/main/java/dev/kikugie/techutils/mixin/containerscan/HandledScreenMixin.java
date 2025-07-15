package dev.kikugie.techutils.mixin.containerscan;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.kikugie.techutils.config.LitematicConfigs;
import dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HandledScreen.class)
public class HandledScreenMixin {

	@Shadow
	protected int x;

	@Shadow
	protected int y;

	@Shadow @Nullable protected Slot focusedSlot;

	@ModifyExpressionValue(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;", ordinal = 0))
	private ItemStack injectTransparency(ItemStack stack, @Local(argsOnly = true) DrawContext context, @Local(argsOnly = true) Slot slot) {
		return InventoryOverlay.drawStack(context, slot, stack);
	}

	@Inject(method = "drawSlot", at = @At("RETURN"))
	private void finalizeDraw(CallbackInfo ci) {
		InventoryOverlay.finalizeDrawStack();
	}

	@ModifyExpressionValue(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;hasStack()Z"))
	private boolean tryDrawTooltipOfSchematicItem(boolean hasStack, @Share("prevItem") LocalRef<ItemStack> prevItemRef) {
		var prevItem = focusedSlot.getStack();
		if ((!hasStack || LitematicConfigs.FORCE_SCHEMATIC_ITEM_OVERLAY.getBooleanValue())
			&& InventoryOverlay.setSlotToSchematicItem(focusedSlot)
		) {
			hasStack = focusedSlot.hasStack();
			if (hasStack) {
				prevItemRef.set(prevItem);
			} else {
				focusedSlot.setStack(prevItem);
			}
		}
		return hasStack;
	}

	@Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
	private void trySetFocusedSlotBackToPrev(CallbackInfo ci, @Share("prevItem") LocalRef<ItemStack> prevItemRef) {
		var prevItem = prevItemRef.get();
		if (prevItem != null) {
			focusedSlot.setStack(prevItem);
		}
	}
}
