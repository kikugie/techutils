package dev.kikugie.techutils.mixin.worldeditsync;

import com.sk89q.worldedit.fabric.net.handler.WECUIPacketHandler;
import dev.kikugie.techutils.feature.worldedit.WorldEditSync;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = WECUIPacketHandler.class, remap = false)
public class WECUIPacketHandlerMixin {
	@Inject(method = "init", at = @At("RETURN"))
	private static void initWESync(CallbackInfo ci) {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> WorldEditSync.init());
	}
}
