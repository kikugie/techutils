package dev.kikugie.techutils.mixin.worldeditsync;

import com.mojang.brigadier.CommandDispatcher;
import dev.kikugie.techutils.feature.worldedit.WorldEditSync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	private CommandDispatcher<CommandSource> commandDispatcher;

	@Inject(method = "onCommandTree", at = @At("RETURN"))
	private void registerWorldEdit(CommandTreeS2CPacket packet, CallbackInfo ci) {
		WorldEditSync.getInstance().ifPresent(instance -> instance.onCommandTreePacket(this.commandDispatcher));
	}
}
