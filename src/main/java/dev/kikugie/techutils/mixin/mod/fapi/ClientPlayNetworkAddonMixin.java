package dev.kikugie.techutils.mixin.mod.fapi;

import dev.kikugie.techutils.feature.worldedit.WorldEditNetworkHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.network.packet.CustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ClientPlayNetworkAddon.class)
public class ClientPlayNetworkAddonMixin {
	/**
	 * Prevents packet receiver collision with other WorldEdit addons, such as WorldEditCUI.
	 */
	@Inject(method = "lambda$receive$0", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/client/networking/v1/ClientPlayNetworking$PlayPayloadHandler;receive(Lnet/minecraft/network/packet/CustomPayload;Lnet/fabricmc/fabric/api/client/networking/v1/ClientPlayNetworking$Context;)V"))
	private void yoinkWorldEditPacket(ClientPlayNetworking.PlayPayloadHandler<?> handler, CustomPayload payload, CallbackInfo ci) {
		if (payload.getId().id().equals(WorldEditNetworkHandler.CHANNEL))
			WorldEditNetworkHandler.getInstance().ifPresent(handlerInstance -> handlerInstance.onYoinkedPacket(payload));
	}
}
