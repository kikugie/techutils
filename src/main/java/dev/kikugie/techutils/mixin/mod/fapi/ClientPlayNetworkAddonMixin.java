package dev.kikugie.techutils.mixin.mod.fapi;

import dev.kikugie.techutils.feature.worldedit.WorldEditNetworkHandler;
import net.fabricmc.fabric.impl.networking.payload.ResolvablePayload;
import net.fabricmc.fabric.impl.networking.payload.RetainedPayload;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientPlayNetworkAddonMixin {
	/**
	 * Prevents packet receiver collision with other WorldEdit addons, such as WorldEditCUI.
	 */
	@SuppressWarnings("UnstableApiUsage")
    @Inject(method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V", at = @At("HEAD"), cancellable = true)
	private void yoinkWorldEditPacket(CustomPayloadS2CPacket packet, CallbackInfo cir) {
		if (packet.payload().id().equals(WorldEditNetworkHandler.CHANNEL)) {
			if (packet.payload() instanceof RetainedPayload) {
				WorldEditNetworkHandler.getInstance().ifPresent(handlerInstance -> handlerInstance.onYoinkedPacket(packet));
			}
		}
	}
}
