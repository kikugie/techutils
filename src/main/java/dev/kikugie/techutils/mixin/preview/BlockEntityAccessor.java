package dev.kikugie.techutils.mixin.preview;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntity.class)
@Environment(EnvType.CLIENT)
public interface BlockEntityAccessor {
    @Accessor("cachedState")
    void setCachedState(BlockState state);
}
