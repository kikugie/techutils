package me.kikugie.techutils.mixin;

import me.kikugie.techutils.feature.inverifier.VerifierRecorder;
import me.kikugie.techutils.networking.GamerQueryHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onOpenScreen", at = @At("HEAD"))
    private void onOpenScreenListener(OpenScreenS2CPacket packet, CallbackInfo ci) {
        VerifierRecorder.onScreenPacket(packet.getSyncId());
    }

    @Inject(method = "onInventory", at = @At("HEAD"))
    private void onInventoryListener(InventoryS2CPacket packet, CallbackInfo ci) {
        VerifierRecorder.onInventoryPacket(packet.getSyncId(), packet.getContents());
    }

    @Inject(method = "onNbtQueryResponse", at = @At("HEAD"))
    private void onGamerQueryResponse(NbtQueryResponseS2CPacket packet, CallbackInfo ci) {
        if (packet.getTransactionId() < 0) {
            GamerQueryHandler.INSTANCE.handleQueryResponse(packet.getTransactionId(), packet.getNbt());
        }
    }
}
