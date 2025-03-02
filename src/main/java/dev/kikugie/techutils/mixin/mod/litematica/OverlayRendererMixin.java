package dev.kikugie.techutils.mixin.mod.litematica;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.render.OverlayRenderer;
import fi.dy.masa.malilib.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(OverlayRenderer.class)
public class OverlayRendererMixin {
	@ModifyArg(method = "renderVerifierOverlay", at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/render/RenderUtils;renderInventoryOverlays(Lfi/dy/masa/litematica/util/BlockInfoAlignment;ILnet/minecraft/world/World;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/DrawContext;)I"), index = 3)
	private World provideBestWorld(World world, @Local(argsOnly = true) MinecraftClient mc) {
		return WorldUtils.getBestWorld(mc);
	}
}
