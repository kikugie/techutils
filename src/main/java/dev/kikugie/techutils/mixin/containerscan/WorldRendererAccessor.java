package dev.kikugie.techutils.mixin.containerscan;

import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {

    @Accessor
    PostEffectProcessor getEntityOutlinePostProcessor();

    @Accessor
    BufferBuilderStorage getBufferBuilders();
}
