package dev.kikugie.techutils.mixin.client;

import dev.kikugie.techutils.client.task.TaskManager;
import dev.kikugie.techutils.client.feature.containerscan.screen.ScreenBlocker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        TaskManager.INSTANCE.tick();
    }

    @Inject(method = {"disconnect()V", "setWorld"}, at = @At("RETURN"))
    private void onDisconnect(CallbackInfo ci) {
        TaskManager.INSTANCE.onUnload();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onScreen(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof ScreenHandlerProvider<?>))
            return;

        if (ScreenBlocker.Companion.onScreen(screen))
            ci.cancel();
    }
}
