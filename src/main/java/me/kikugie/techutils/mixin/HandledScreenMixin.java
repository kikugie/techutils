package me.kikugie.techutils.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.kikugie.techutils.feature.inverifier.ContainerStorage;
import me.kikugie.techutils.feature.inverifier.VerifierRecorder;
import me.kikugie.techutils.render.TransparencyBuffer;
import me.kikugie.techutils.render.gui.LitematicInventoryRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HandledScreen.class, priority = 900)
public abstract class HandledScreenMixin extends Screen {
    @Shadow
    protected int x;
    @Shadow
    protected int y;

    private Slot renderSlot;
    @Nullable
    private LitematicInventoryRenderer litematicItemRenderer = null;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initLitematicInventory(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        if (!me.kikugie.techutils.config.Configs.LitematicConfigs.INVENTORY_SCREEN_OVERLAY.getBooleanValue()) return;

        VerifierRecorder.Entry entry = VerifierRecorder.getActive();
        if (entry != null) {
            SimpleInventory schematicInventory = ContainerStorage.getSchematicInventory(entry.pos(), entry.state());
            if (schematicInventory != null) {
                litematicItemRenderer = new LitematicInventoryRenderer(handler, schematicInventory);
            }
        }
    }


    @Inject(method = "close", at = @At("TAIL"))
    private void resetRecorder(CallbackInfo ci) {
        VerifierRecorder.close();
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void saveSlot(MatrixStack stack, Slot slot, CallbackInfo ci) {
        renderSlot = slot;
    }

    @ModifyVariable(method = "drawSlot", at = @At(value = "STORE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack renderItem(ItemStack stack, MatrixStack matrices) {
        if (litematicItemRenderer != null) {
            return litematicItemRenderer.drawStack(matrices, x, y, renderSlot, stack);
        }
        return stack;
    }

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void postRenderItem(MatrixStack matrices, Slot slot, CallbackInfo ci) {
        if (litematicItemRenderer != null) {
            litematicItemRenderer.drawTransparencyBuffer(matrices, x, y);
        }
    }
}
