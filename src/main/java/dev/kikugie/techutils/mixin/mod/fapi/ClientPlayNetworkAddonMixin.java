package dev.kikugie.techutils.mixin.mod.fapi;

import dev.kikugie.techutils.feature.worldedit.WorldEditNetworkHandler;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.fabricmc.fabric.impl.networking.payload.ResolvedPayload;
import net.fabricmc.fabric.impl.networking.payload.UntypedPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = ClientPlayNetworkAddon.class, remap = false)
public class ClientPlayNetworkAddonMixin {
	/**
	 * Prevents packet receiver collision with other WorldEdit addons, such as WorldEditCUI.
	 */
    @Inject(method = "receive(Lnet/fabricmc/fabric/impl/networking/client/ClientPlayNetworkAddon$Handler;Lnet/fabricmc/fabric/impl/networking/payload/ResolvedPayload;)V", at = @At("HEAD"))
	private void yoinkWorldEditPacket(ClientPlayNetworkAddon.Handler handler, ResolvedPayload payload, CallbackInfo ci) {
		if (payload instanceof UntypedPayload untypedPayload
			&& payload.id().equals(WorldEditNetworkHandler.CHANNEL)
		) {
			WorldEditNetworkHandler.getInstance().ifPresent(handlerInstance ->
				handlerInstance.onYoinkedPacket(untypedPayload.buffer())
			);
		}
	}
}
