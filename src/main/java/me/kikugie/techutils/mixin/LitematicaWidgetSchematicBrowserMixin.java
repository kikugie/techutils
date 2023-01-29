package me.kikugie.techutils.mixin;

import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import me.kikugie.techutils.config.Configs;
import me.kikugie.techutils.render.litematica.LitematicRenderManager;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.Map;

@Mixin(WidgetSchematicBrowser.class)
class LitematicaWidgetSchematicBrowserMixin {
    @Final
    @Shadow(remap = false)
    protected Map<File, Pair<Identifier, NativeImageBackedTexture>> cachedPreviewImages;

    @Inject(method = "drawSelectedSchematicInfo", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void drawPreview(WidgetFileBrowserBase.DirectoryEntry entry, MatrixStack matrixStack, CallbackInfo ci, int x, int y, int height) {
        if (!Configs.LitematicConfigs.getRENDER_PREVIEW().getBooleanValue()) {
            return;
        }
        if (!Configs.LitematicConfigs.getOVERRIDE_PREVIEW().getBooleanValue() && cachedPreviewImages.containsKey(entry.getFullPath())) {
            return;
        }
        LitematicRenderManager.getInstance().setCurrentRenderer(entry);
        LitematicRenderManager.getInstance().renderCurrent(x + 4, y + 14, height - y - 2);
        ci.cancel();
    }
}
