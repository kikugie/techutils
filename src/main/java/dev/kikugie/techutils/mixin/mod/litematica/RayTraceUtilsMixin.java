package dev.kikugie.techutils.mixin.mod.litematica;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.kikugie.techutils.config.LitematicConfigs;
import fi.dy.masa.litematica.util.RayTraceUtils;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RayTraceUtils.class)
public class RayTraceUtilsMixin {
	@ModifyExpressionValue(method = {"traceFirstStep", "traceLoopSteps"}, at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/block/BlockState;getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"
	))
	private static VoxelShape useFullCube(VoxelShape original) {
		return LitematicConfigs.EASY_PLACE_FULL_BLOCKS.getBooleanValue() && !original.isEmpty() ? VoxelShapes.fullCube() : original;
	}
}
