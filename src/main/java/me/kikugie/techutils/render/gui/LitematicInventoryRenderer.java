package me.kikugie.techutils.render.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.litematica.config.Configs;
import me.kikugie.techutils.access.DrawableHelperAccessor;
import me.kikugie.techutils.render.TransparencyBuffer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class LitematicInventoryRenderer extends DrawableHelper {
    private final int MISSING_COLOR = Configs.Colors.SCHEMATIC_OVERLAY_COLOR_MISSING.getIntegerValue();
    private final int WRONG_COLOR = Configs.Colors.SCHEMATIC_OVERLAY_COLOR_WRONG_BLOCK.getIntegerValue();
    private final int MISMATCHED_COLOR = Configs.Colors.SCHEMATIC_OVERLAY_COLOR_WRONG_STATE.getIntegerValue();
    private final int EXTRA_COLOR = Configs.Colors.SCHEMATIC_OVERLAY_COLOR_EXTRA.getIntegerValue();
    private final Inventory inventory;
    private boolean renderCurrentItemTransparent = false;

    public LitematicInventoryRenderer(Inventory inventory) {
        this.inventory = inventory;
    }

    public ItemStack drawStack(MatrixStack matrices, int x, int y, Slot slot, ItemStack stack) {
        if (slot.inventory instanceof PlayerInventory) return stack;

        ItemStack schematicStack = inventory.getStack(slot.getIndex());
        if (schematicStack == null) {
            schematicStack = ItemStack.EMPTY;
        }

        int color = 0;
        if (stack.isEmpty() && !schematicStack.isEmpty()) {
            color = MISSING_COLOR;
            stack = schematicStack;
            renderCurrentItemTransparent = true;
        } else if (!stack.isEmpty() && schematicStack.isEmpty()) {
            color = EXTRA_COLOR;
        } else if (!stack.getItem().equals(schematicStack.getItem())) {
            color = WRONG_COLOR;
        } else if (stack.getCount() != schematicStack.getCount()) {
            color = MISMATCHED_COLOR;
        }

        if (color != 0) {
            drawBackground(matrices, x, y, slot, color);
        }

        if (renderCurrentItemTransparent) {
            TransparencyBuffer.prepareExtraFramebuffer();
        }

        return stack;
    }

    public void drawBackground(MatrixStack matrices, int x, int y, Slot slot, int color) {

        matrices.push();
        matrices.loadIdentity();

        RenderSystem.disableDepthTest();
        RenderSystem.colorMask(true, true, true, false);
        ((DrawableHelperAccessor) this).fillGradientSafe(matrices, slot.x, slot.y, slot.x + 16, slot.y + 16, color, color);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableDepthTest();

        matrices.pop();
    }

    public void drawTransparencyBuffer(MatrixStack matrices, int x, int y) {
        if (renderCurrentItemTransparent) {
            renderCurrentItemTransparent = false;
            TransparencyBuffer.preInject();

            // Align the matrix stack
            matrices.push();
            matrices.translate(-x, -y, 0);

            // Draw the framebuffer texture
            TransparencyBuffer.drawExtraFramebuffer(matrices);
            matrices.pop();

            TransparencyBuffer.postInject();
        }
    }
}
