package dev.kikugie.techutils.mixin.mod.litematica.widget;

import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Restriction(require = @Condition("isometric-renders"))
@Mixin(value = WidgetSchematicBrowser.class, remap = false)
public class PreviewWidgetSchematicBrowserMixin {
    @Shadow
    @Final
    protected GuiSchematicBrowserBase parent;

//    @ModifyExpressionValue(method = "drawSelectedSchematicInfo", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
//    private Object drawPreview(
//            Object original,
//            @Local(argsOnly = true) @Nullable WidgetFileBrowserBase.DirectoryEntry entry,
//            @Local(ordinal = 0) int x,
//            @Local(ordinal = 1) int y,
//            @Local(ordinal = 2) int height
//    ) {
//        if (!PreviewConfig.renderPreview.getBooleanValue())
//            return original;
//        if (!PreviewConfig.overridePreview.getBooleanValue() && original != null)
//            return original;
////        ((PreviewManager.Accessor) this.parent).getPreviewManager().drawPreview(entry, ((ScissorStack.Provider) this).getScissorStack(), x + 4, y + 14, height - y - 2);
//        return null;
//    }
}
