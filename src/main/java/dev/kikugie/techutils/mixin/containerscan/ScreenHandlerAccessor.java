package dev.kikugie.techutils.mixin.containerscan;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {
    @Accessor("type")
    ScreenHandlerType<?> getNullableType();
}
