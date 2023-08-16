package dev.kikugie.techutils.mixin.preview;

import dev.kikugie.techutils.feature.preview.model.PreviewFluidRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#if MC <= 11802
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif

/**
 * @see <a href="https://github.com/gliscowo/worldmesher/blob/1.19/src/main/java/io/wispforest/worldmesher/mixin/MixinFluidRendererMixin.java">Source</a>
 */
@Mixin(value = FluidRenderer.class, priority = 1100)
public class PostFluidRendererMixin {
    @SuppressWarnings("MixinAnnotationTarget")
    @Shadow(remap = false)
    @Final
    private ThreadLocal<Boolean> fabric_customRendering;

    @SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget", "CancellableInjectionUsage", "ConstantConditions"})
    @Inject(method = "tessellateViaHandler", at = @At("HEAD"), cancellable = true, remap = false)
    private void noNotMyJays(BlockRenderView view,
                             BlockPos pos,
                             VertexConsumer vertexConsumer,
                             BlockState blockState,
                             FluidState fluidState,
                             //#if MC > 11802
                             CallbackInfo delegateInfo,
                             //#else
                             //$$ CallbackInfoReturnable<Boolean> delegateInfo,
                             //#endif
                             CallbackInfo info) {
        if ((Object) this instanceof PreviewFluidRenderer) {
            this.fabric_customRendering.set(true);
            info.cancel();
        }
    }

}
