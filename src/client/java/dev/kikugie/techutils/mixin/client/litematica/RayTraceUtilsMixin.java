package dev.kikugie.techutils.mixin.client.litematica;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.kikugie.techutils.config.LitematicaMisc;
import fi.dy.masa.litematica.util.RayTraceUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(RayTraceUtils.class)
public class RayTraceUtilsMixin {
    @ModifyExpressionValue(method = {"traceFirstStep", "traceLoopSteps"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;"))
    private static VoxelShape useFullCube(VoxelShape original) {
        return !original.isEmpty() && LitematicaMisc.INSTANCE.getEasyPlaceFullBlocks().getBooleanValue() ? VoxelShapes.fullCube() : original;
    }
}
