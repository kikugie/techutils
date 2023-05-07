package me.kikugie.techutils.mixin;

import me.kikugie.techutils.access.DrawableHelperAccessor;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DrawableHelper.class)
public abstract class DrawableHelperMixin implements DrawableHelperAccessor {
    @Shadow
    protected void fillGradient(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
    }

    @Override
    public void fillGradientSafe(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
        fillGradient(matrices, startX, startY, endX, endY, colorStart, colorEnd);
    }
}
