package dev.kikugie.techutils.mixin.containerscan;

import dev.kikugie.techutils.feature.containerscan.handlers.InteractionHandler;
import dev.kikugie.techutils.render.TransparencyBuffer;
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
    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    private void resizeDisplay(CallbackInfo ci) {
        TransparencyBuffer.resizeDisplay();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onScreen(Screen screen, CallbackInfo ci) {
        if (!(screen instanceof ScreenHandlerProvider<?>))
            return;

        if (!InteractionHandler.onScreen(screen))
            ci.cancel();
    }
}
