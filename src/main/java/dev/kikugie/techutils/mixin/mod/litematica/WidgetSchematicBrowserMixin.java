package dev.kikugie.techutils.mixin.mod.litematica;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.techutils.client.feature.preview.PreviewConfig;
import dev.kikugie.techutils.client.feature.preview.render.PreviewInvoker;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(require = @Condition("isometric-renders"))
@Mixin(value = WidgetSchematicBrowser.class, remap = false)
public class WidgetSchematicBrowserMixin {
    @Shadow @Final protected GuiSchematicBrowserBase parent;

    @ModifyExpressionValue(method = "drawSelectedSchematicInfo", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object drawPreview(
            Object original,
            @Local(argsOnly = true) @Nullable WidgetFileBrowserBase.DirectoryEntry entry,
            @Local(argsOnly = true) DrawContext drawContext,
            @Local(ordinal = 0) int x,
            @Local(ordinal = 1) int y,
            @Local(ordinal = 2) int height
    ) {
        if (!PreviewConfig.renderPreview.getBooleanValue())
            return original;
        if (!PreviewConfig.overridePreview.getBooleanValue() && original != null)
            return original;
        ((PreviewInvoker) this.parent).drawPreview(entry, drawContext, x + 4, y + 14, height - y - 2);
        return null;
    }
}
