package dev.kikugie.techutils.mixin.preview;

import dev.kikugie.techutils.feature.preview.model.PreviewFluidRenderer;
import net.minecraft.client.render.block.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric API causes liquid rendering to break, so we need to cancel the resource reload event for preview rendering.
 *
 * @see <a href="https://github.com/gliscowo/worldmesher/blob/1.19/src/main/java/io/wispforest/worldmesher/mixin/FluidRendererMixin.java">Source</a>
 */
@Mixin(value = FluidRenderer.class, priority = 900)
public class PreFluidRendererMixin {
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onResourceReload", at = @At("RETURN"), cancellable = true)
    private void imGonnaCreaseYourJaysFabric(CallbackInfo ci) {
        if ((Object) this instanceof PreviewFluidRenderer) ci.cancel();
    }
}
