package dev.kikugie.techutils.mixin.containerscan;

import dev.kikugie.techutils.util.EntitiesDataStorage;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onNbtQueryResponse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/DataQueryHandler;handleQueryResponse(ILnet/minecraft/nbt/NbtCompound;)Z"))
	private void onQueryResponse(NbtQueryResponseS2CPacket packet, CallbackInfo ci)
	{
		EntitiesDataStorage.getInstance().handleVanillaQueryNbt(packet.getTransactionId(), packet.getNbt());
	}

	@Inject(method = "onCommandTree", at = @At("RETURN"))
	private void onCommandTree(CallbackInfo ci)
	{
		// when the player becomes OP, the server sends the command tree to the client
		EntitiesDataStorage.getInstance().resetOpCheck();
	}
}
