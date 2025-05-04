package dev.kikugie.techutils.mixin.mod.malilib;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.infoOverlayInstance;
import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.mousePos;
import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.hoveredStackToRender;

@Mixin(value = fi.dy.masa.malilib.render.InventoryOverlay.class)
public class InventoryOverlayMixin {
	@WrapOperation(method = "renderInventoryStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;getStack(I)Lnet/minecraft/item/ItemStack;"))
	private static ItemStack shareSlotIndex(Inventory instance, int i, Operation<ItemStack> original, @Share("slotIndex") LocalIntRef slotIndex) {
		slotIndex.set(i);
		return original.call(instance, i);
	}

	@WrapOperation(method = "renderInventoryStacks", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/InventoryOverlay;renderStackAt(Lnet/minecraft/item/ItemStack;FFFLnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/DrawContext;)V"))
	private static void drawOverlay(ItemStack stack, float x, float y, float scale, MinecraftClient mc, DrawContext drawContext, Operation<Void> original, @Share("slotIndex") LocalIntRef slotIndex) {
		if (infoOverlayInstance != null) {
			stack = infoOverlayInstance.drawStackInternal(drawContext, new Slot(null, slotIndex.get(), (int) x, (int) y), stack);

			original.call(stack, x, y, scale, mc, drawContext);

			infoOverlayInstance.drawTransparencyBufferInternal(drawContext, 0, 0);
		} else {
			original.call(stack, x, y, scale, mc, drawContext);
		}
	}

	@Redirect(method = "renderInventoryStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
	private static boolean allowDrawingEmptySlots(ItemStack instance) {
		return false;
	}

	@Inject(method = "renderInventoryStacks", at = @At("RETURN"))
	private static void cleanUp(CallbackInfo ci) {
		infoOverlayInstance = null;
	}

	@Inject(method = "renderStackAt", at = @At("TAIL"))
	private static void storeIfHovered(ItemStack stack, float x, float y, float scale, MinecraftClient mc, DrawContext drawContext, CallbackInfo ci) {
		if (mousePos.x >= x && mousePos.x < x + 16 * scale && mousePos.y >= y && mousePos.y < y + 16 * scale
			&& !stack.isEmpty()
		) {
			hoveredStackToRender = stack.copy();
		}
	}

	@Redirect(method = "renderStackToolTip", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawHoverText(IILjava/util/List;Lnet/minecraft/client/gui/DrawContext;)V"))
	private static void useBetterRenderingMethod(int x, int y, List<String> unstyledLines, DrawContext drawContext, @Local(ordinal = 0) List<Text> lines) {
		drawContext.drawTooltip(MinecraftClient.getInstance().textRenderer, lines, x, y);
	}
}
