package dev.kikugie.techutils.mixin.mod.malilib.gui;

import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(value = ButtonGeneric.class, remap = false)
public class ButtonGenericMixin {
    @Shadow protected LeftRight alignment;

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawTexturedRect(IIIIII)V"), index = 0)
    private int centerButton(int x) {
        return this.alignment == LeftRight.CENTER ? x + 2 : x;
    }
}
