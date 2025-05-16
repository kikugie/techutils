package dev.kikugie.techutils.mixin.mod.litematica;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.kikugie.techutils.feature.containerscan.LinkedStorageEntry;
import dev.kikugie.techutils.feature.containerscan.verifier.BlockMismatchExtension;
import fi.dy.masa.litematica.gui.GuiSchematicVerifier;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicVerificationResult;
import fi.dy.masa.litematica.render.OverlayRenderer;
import fi.dy.masa.litematica.render.RenderUtils;
import fi.dy.masa.litematica.util.BlockInfoAlignment;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntrySortable;
import fi.dy.masa.malilib.render.InventoryOverlay;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.game.BlockUtils;
import fi.dy.masa.malilib.util.nbt.NbtBlockUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.Set;

import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.delayRenderingHoveredStack;
import static dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay.hoveredStackToRender;
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

		InventoryBE left = inventories.getLeft();
		InventoryBE right = inventories.getRight();
		if (infoOverlay == null) {
			infoOverlay = new dev.kikugie.techutils.feature.containerscan.verifier.InventoryOverlay(new LinkedStorageEntry(BlockPos.ORIGIN, right, left));
		}

		delayRenderingHoveredStack = true;

		renderInventoryOverlay(BlockInfoAlignment.CENTER, LeftRight.LEFT, 0, mc, drawContext, left, mouseX, mouseY);

		infoOverlayInstance = infoOverlay;
		renderInventoryOverlay(BlockInfoAlignment.CENTER, LeftRight.RIGHT, 0, mc, drawContext, right, mouseX, mouseY);
		infoOverlayInstance = null;

		delayRenderingHoveredStack = false;

		if (hoveredStackToRender != null) {
			InventoryOverlay.renderStackToolTipStyled(mouseX, mouseY, hoveredStackToRender, mc, drawContext);
			hoveredStackToRender = null;
		}

		return false;
	}

	/**
	 * Basically a clone of {@link RenderUtils#renderInventoryOverlay(BlockInfoAlignment, LeftRight, int, World, BlockPos, MinecraftClient, DrawContext)}
	 */
	@Unique
	private int renderInventoryOverlay(BlockInfoAlignment align, LeftRight side, int offY,
											 MinecraftClient mc, DrawContext drawContext, InventoryBE inventoryBE,
											 double mouseX, double mouseY)
	{
		var nbt = inventoryBE.createNbtWithIdentifyingData(mc.world.getRegistryManager());
		InventoryOverlay.Context ctx = new InventoryOverlay.Context(InventoryOverlay.getBestInventoryType(inventoryBE, nbt), inventoryBE, inventoryBE, null, nbt, null);

		if (ctx.inv() != null)
		{
			final InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(ctx.type(), ctx.inv().size());

//            Litematica.LOGGER.error("render(): type [{}], inv [{}], be [{}], nbt [{}]", ctx.type().name(), ctx.inv().size(), ctx.be() != null, ctx.nbt() != null ? ctx.nbt().getString("id") : new NbtCompound());

			// Try to draw Locked Slots on Crafter Grid
			if (ctx.type() == InventoryOverlay.InventoryRenderType.CRAFTER)
			{
				Set<Integer> disabledSlots = new HashSet<>();

				if (ctx.nbt() != null && !ctx.nbt().isEmpty())
				{
					disabledSlots = NbtBlockUtils.getDisabledSlotsFromNbt(ctx.nbt());
				}
				else if (ctx.be() instanceof CrafterBlockEntity cbe)
				{
					disabledSlots = BlockUtils.getDisabledSlots(cbe);
				}

				return renderInventoryOverlay(align, side, offY, ctx.inv(), ctx.type(), props, disabledSlots, mc, drawContext, mouseX, mouseY);
			}
			else
			{
				return renderInventoryOverlay(align, side, offY, ctx.inv(), ctx.type(), props, Set.of(), mc, drawContext, mouseX, mouseY);
			}
		}

		return 0;
	}

	/**
	 * Basically a clone of {@link RenderUtils#renderInventoryOverlay(BlockInfoAlignment, LeftRight, int, Inventory, InventoryOverlay.InventoryRenderType, InventoryOverlay.InventoryProperties, Set, MinecraftClient, DrawContext)}
	 */
	@Unique
	private static int renderInventoryOverlay(BlockInfoAlignment align, LeftRight side, int offY,
											  Inventory inv, InventoryOverlay.InventoryRenderType type, InventoryOverlay.InventoryProperties props, Set<Integer> disabledSlots,
											  MinecraftClient mc, DrawContext drawContext, double mouseX, double mouseY)
	{
		int xInv = 0;
		int yInv = 0;
		int compatShift = OverlayRenderer.calculateCompatYShift();

		switch (align)
		{
			case CENTER:
				xInv = GuiUtils.getScaledWindowWidth() / 2 - (props.width / 2);
				yInv = GuiUtils.getScaledWindowHeight() / 2 - props.height - offY;
				break;
			case TOP_CENTER:
				xInv = GuiUtils.getScaledWindowWidth() / 2 - (props.width / 2);
                yInv = offY + compatShift;
				break;
		}

		if      (side == LeftRight.LEFT)  { xInv -= (props.width / 2 + 4); }
		else if (side == LeftRight.RIGHT) { xInv += (props.width / 2 + 4); }

		fi.dy.masa.malilib.render.RenderUtils.color(1f, 1f, 1f, 1f);

		InventoryOverlay.renderInventoryBackground(type, xInv, yInv, props.slotsPerRow, props.totalSlots, mc, drawContext);
		InventoryOverlay.renderInventoryStacks(type, inv, xInv + props.slotOffsetX, yInv + props.slotOffsetY, props.slotsPerRow, 0, inv.size(), disabledSlots, mc, drawContext, mouseX, mouseY);

        return props.height + compatShift;
	}
}
