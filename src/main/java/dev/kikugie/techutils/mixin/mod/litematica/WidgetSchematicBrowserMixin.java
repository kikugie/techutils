package dev.kikugie.techutils.mixin.mod.litematica;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.techutils.config.LitematicConfigs;
import dev.kikugie.techutils.feature.preview.gui.PreviewRenderManager;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WidgetSchematicBrowser.class, remap = false)
public abstract class WidgetSchematicBrowserMixin {
    @ModifyExpressionValue(method = "drawSelectedSchematicInfo", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object drawPreview(
            Object original,
            @Local(argsOnly = true) @Nullable WidgetFileBrowserBase.DirectoryEntry entry,
            @Local(argsOnly = true) DrawContext drawContext,
            @Local(ordinal = 0) int x,
            @Local(ordinal = 1) int y,
            @Local(ordinal = 2) int height
    ) {
        if (!LitematicConfigs.RENDER_PREVIEW.getBooleanValue())
            return original;
        if (!LitematicConfigs.OVERRIDE_PREVIEW.getBooleanValue() && original != null)
            return original;
        PreviewRenderManager.getInstance().ifPresent(manager -> manager.getOrCreateRenderer(entry).render(drawContext, x + 4, y + 14, height - y - 2));
        return null;
    }
}
