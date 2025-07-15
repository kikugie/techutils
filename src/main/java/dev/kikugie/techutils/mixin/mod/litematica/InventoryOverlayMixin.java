package dev.kikugie.techutils.mixin.mod.litematica;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.delayRenderingHoveredStack;
import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.hoveredStackToRender;
import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.infoOverlayInstance;

@Mixin(value = fi.dy.masa.malilib.render.InventoryOverlay.class)
public class InventoryOverlayMixin {
	@WrapOperation(method = "renderInventoryStacks(Lnet/minecraft/client/gui/DrawContext;Lfi/dy/masa/malilib/render/InventoryOverlay$InventoryRenderType;Lnet/minecraft/inventory/Inventory;IIIIILjava/util/Set;Lnet/minecraft/client/MinecraftClient;DD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;getStack(I)Lnet/minecraft/item/ItemStack;"))
	private static ItemStack shareSlotIndex(Inventory instance, int i, Operation<ItemStack> original, @Share("slotIndex") LocalIntRef slotIndex) {
		slotIndex.set(i);
		return original.call(instance, i);
	}

	@WrapOperation(method = "renderInventoryStacks(Lnet/minecraft/client/gui/DrawContext;Lfi/dy/masa/malilib/render/InventoryOverlay$InventoryRenderType;Lnet/minecraft/inventory/Inventory;IIIIILjava/util/Set;Lnet/minecraft/client/MinecraftClient;DD)V", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/InventoryOverlay;renderStackAt(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemStack;FFFLnet/minecraft/client/MinecraftClient;DD)V"))
	private static void drawOverlay(DrawContext drawContext, ItemStack stack, float x, float y, float scale, MinecraftClient mc, double mouseX, double mouseY, Operation<Void> original, @Share("slotIndex") LocalIntRef slotIndex) {
		if (infoOverlayInstance != null) {
			stack = infoOverlayInstance.drawStackInternal(drawContext, new Slot(null, slotIndex.get(), (int) x, (int) y), stack);

			original.call(drawContext, stack, x, y, scale, mc, mouseX, mouseY);

			infoOverlayInstance.finalizeDrawStackInternal();
		} else {
			original.call(drawContext, stack, x, y, scale, mc, mouseX, mouseY);
		}
	}

	@Redirect(method = "renderInventoryStacks(Lnet/minecraft/client/gui/DrawContext;Lfi/dy/masa/malilib/render/InventoryOverlay$InventoryRenderType;Lnet/minecraft/inventory/Inventory;IIIIILjava/util/Set;Lnet/minecraft/client/MinecraftClient;DD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
	private static boolean allowDrawingEmptySlots(ItemStack instance) {
		return false;
	}

	@WrapWithCondition(method = "renderInventoryStacks(Lnet/minecraft/client/gui/DrawContext;Lfi/dy/masa/malilib/render/InventoryOverlay$InventoryRenderType;Lnet/minecraft/inventory/Inventory;IIIIILjava/util/Set;Lnet/minecraft/client/MinecraftClient;DD)V", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/InventoryOverlay;renderStackToolTipStyled(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/item/ItemStack;Lnet/minecraft/client/MinecraftClient;)V"))
	private static boolean delayRenderingHoveredStack(DrawContext drawContext, int x, int y, ItemStack stack, MinecraftClient mc) {
		if (delayRenderingHoveredStack) {
			hoveredStackToRender = stack;
			return false;
		}
		return true;
	}

	@Inject(method = "renderInventoryStacks(Lnet/minecraft/client/gui/DrawContext;Lfi/dy/masa/malilib/render/InventoryOverlay$InventoryRenderType;Lnet/minecraft/inventory/Inventory;IIIIILjava/util/Set;Lnet/minecraft/client/MinecraftClient;DD)V", at = @At("RETURN"))
	private static void cleanUp(CallbackInfo ci) {
		infoOverlayInstance = null;
	}
}
