package me.kikugie.techutils.access;

import net.minecraft.client.util.math.MatrixStack;

public interface DrawableHelperAccessor {
    void fillGradientSafe(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd);
}
