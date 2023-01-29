package me.kikugie.techutils.mixin;

import me.kikugie.techutils.networking.WorldEditNetworkHandler;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkAddon.class)
public class FAPIClientPlayNetworkAddonMixin {
    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/networking/client/ClientPlayNetworkAddon;handle(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Z"))
    private void yoinkWorldEditPacket(CustomPayloadS2CPacket packet, CallbackInfoReturnable<Boolean> cir) {
        if (!packet.getChannel().equals(WorldEditNetworkHandler.getCHANNEL())) return;
        var handlerInstance = WorldEditNetworkHandler.getInstance();
        if (handlerInstance != null) {
            handlerInstance.onYoinkedPacket(packet);
        }
    }
}
