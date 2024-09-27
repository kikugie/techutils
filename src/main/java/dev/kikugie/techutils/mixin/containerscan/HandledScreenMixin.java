package dev.kikugie.techutils.mixin.containerscan;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
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

	@ModifyExpressionValue(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;", ordinal = 0))
	private ItemStack setTransparency(ItemStack stack, @Local(argsOnly = true) DrawContext context, @Local(argsOnly = true) Slot slot) {
		return InventoryOverlay.drawStack(context, slot, stack);
	}

	@Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
	private void restoreTransparency(DrawContext context, Slot slot, CallbackInfo ci) {
		InventoryOverlay.drawTransparencyBuffer(context, this.x, this.y);
	}
}
