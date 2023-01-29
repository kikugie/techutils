package me.kikugie.techutils.mixin;

import me.kikugie.techutils.utils.ResponseMuffler;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public class MessageHandlerMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void interceptMessage(Text message, boolean overlay, CallbackInfo ci) {
        if (ResponseMuffler.matches(message.getString())) {
            ResponseMuffler.pop();
            ci.cancel();
        }
    }
}
