package dev.kikugie.techutils.mixin.mod.litematica;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kikugie.techutils.feature.containerscan.LinkedStorageEntry;
import dev.kikugie.techutils.feature.containerscan.verifier.BlockMismatchExtension;
import fi.dy.masa.litematica.gui.GuiSchematicVerifier;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicVerificationResult;
import fi.dy.masa.litematica.render.RenderUtils;
import fi.dy.masa.litematica.util.BlockInfoAlignment;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntrySortable;
import fi.dy.masa.malilib.render.InventoryOverlay;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.hoveredStackToRender;
import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.mousePos;
import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.infoOverlayInstance;

@Mixin(value = WidgetSchematicVerificationResult.class, remap = false)
public abstract class WidgetSchematicVerificationResultMixin<InventoryBE extends BlockEntity & Inventory> extends WidgetListEntrySortable<GuiSchematicVerifier.BlockMismatchEntry> {
	public WidgetSchematicVerificationResultMixin(int x, int y, int width, int height, @Nullable GuiSchematicVerifier.BlockMismatchEntry entry, int listIndex) {
		super(x, y, width, height, entry, listIndex);
	}

	@Shadow @Final private GuiSchematicVerifier.BlockMismatchEntry mismatchEntry;

	@Shadow protected abstract boolean shouldRenderAsSelected();

	@Unique
	private dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay infoOverlay;

	@WrapWithCondition(method = "postRenderHovered", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/gui/widgets/WidgetSchematicVerificationResult$BlockMismatchInfo;render(IILnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/DrawContext;)V", remap = true))
	private boolean renderInventoryOverlayIfNecessary(WidgetSchematicVerificationResult.BlockMismatchInfo instance, int x, int y, MinecraftClient mc, DrawContext drawContext, int mouseX, int mouseY, boolean selected) {
		//noinspection unchecked
		var inventories = mismatchEntry.blockMismatch == null ? null : ((BlockMismatchExtension<InventoryBE>) mismatchEntry.blockMismatch).getInventories$techutils();
		if (inventories == null) {
			return true;
		}

		if (shouldRenderAsSelected()) {
			if (!selected) {
				return false;
			}
		} else {
			mouseX = 0;
			mouseY = 0;
		}

		mousePos.x = mouseX;
		mousePos.y = mouseY;

		InventoryBE left = inventories.getLeft();
		InventoryBE right = inventories.getRight();
		if (infoOverlay == null) {
			infoOverlay = new dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay(new LinkedStorageEntry(BlockPos.ORIGIN, right, left));
		}

		MatrixStack modelViewStack = RenderSystem.getModelViewStack();

		modelViewStack.push();

		modelViewStack.translate(0, 0, 256);

		InventoryOverlay.InventoryRenderType leftType = InventoryOverlay.getInventoryType(left);
		InventoryOverlay.InventoryProperties leftProps = InventoryOverlay.getInventoryPropsTemp(leftType, left.size());
		RenderUtils.renderInventoryOverlay(BlockInfoAlignment.CENTER, LeftRight.LEFT, 0, left, leftType, leftProps, mc, drawContext);

		infoOverlayInstance = infoOverlay;
		InventoryOverlay.InventoryRenderType rightType = InventoryOverlay.getInventoryType(right);
		InventoryOverlay.InventoryProperties rightProps = InventoryOverlay.getInventoryPropsTemp(rightType, right.size());
		RenderUtils.renderInventoryOverlay(BlockInfoAlignment.CENTER, LeftRight.RIGHT, 0, right, rightType, rightProps, mc, drawContext);
		infoOverlayInstance = null;

		if (hoveredStackToRender != null) {
			modelViewStack.translate(0, 0, 10);

			InventoryOverlay.renderStackToolTip(mouseX, mouseY, hoveredStackToRender, mc, drawContext);
			hoveredStackToRender = null;
		}

		modelViewStack.pop();

		return false;
	}
}
