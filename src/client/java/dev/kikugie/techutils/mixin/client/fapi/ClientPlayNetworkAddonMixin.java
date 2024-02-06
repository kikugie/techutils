package dev.kikugie.techutils.mixin.client.fapi;

import dev.kikugie.techutils.impl.worldedit.WorldEditNetworkHandler;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ClientPlayNetworkAddon.class)
public class ClientPlayNetworkAddonMixin {
    /**
     * Prevents packet receiver collision with other WorldEdit addons, such as WorldEditCUI.
     */
    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/networking/client/ClientPlayNetworkAddon;handle(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Z"))
    private void yoinkWorldEditPacket(CustomPayloadS2CPacket packet, CallbackInfoReturnable<Boolean> cir) {
        if (!packet.getChannel().equals(WorldEditNetworkHandler.Companion.getCHANNEL())) return;
        var handler = WorldEditNetworkHandler.Companion.getINSTANCE();
        if (handler != null) handler.onDataPacket(packet.getData());
    }
}