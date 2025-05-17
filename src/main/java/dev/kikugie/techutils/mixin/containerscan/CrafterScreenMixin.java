package dev.kikugie.techutils.mixin.containerscan;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay;
import net.minecraft.client.gui.screen.ingame.CrafterScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrafterScreen.class)
public abstract class CrafterScreenMixin extends HandledScreen<CrafterScreenHandler> {
	public CrafterScreenMixin(CrafterScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	/**
	 * This method complements {@link HandledScreenMixin#tryDrawTooltipOfSchematicItem(boolean, LocalRef)}
	 * because without it the "Click to disable slot" tooltip will clash with the forced schematic item tooltip.
	 */
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CrafterScreen;drawMouseoverTooltip(Lnet/minecraft/client/gui/DrawContext;II)V"))
	private void tryDrawTooltipOfMissingItem(CallbackInfo ci, @Share("didSetItem") LocalBooleanRef didSetItem) {
		if (focusedSlot != null && focusedSlot.getStack().isEmpty()
			&& InventoryOverlay.setSlotToSchematicItem(focusedSlot)
		) {
			didSetItem.set(true);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void trySetFocusedSlotBackToEmpty(CallbackInfo ci, @Share("didSetItem") LocalBooleanRef didSetItem) {
		if (didSetItem.get()) {
			focusedSlot.setStack(ItemStack.EMPTY);
		}
	}
}
