package dev.kikugie.techutils.mixin.mod.litematica.gui;

import dev.kikugie.techutils.client.feature.browser.BrowserConfig;
import dev.kikugie.techutils.client.feature.browser.widget.StructureBrowserWidget;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stores the preview render manager in the schematic browser. Previews are reset when the browser is closed.
 */
@Restriction(require = @Condition("isometric-renders"))
@Mixin(value = GuiSchematicBrowserBase.class, remap = false)
public abstract class GuiSchematicBrowserBaseMixin extends GuiListBase<WidgetFileBrowserBase.DirectoryEntry, WidgetDirectoryEntry, WidgetSchematicBrowser> {

    protected GuiSchematicBrowserBaseMixin(int listX, int listY) {
        super(listX, listY);
    }

//    @Redirect(method = "createListWidget(II)Lfi/dy/masa/litematica/gui/widgets/WidgetSchematicBrowser;", at = @At(value = "NEW", target = "(IIIILfi/dy/masa/litematica/gui/GuiSchematicBrowserBase;Lfi/dy/masa/malilib/gui/interfaces/ISelectionListener;)Lfi/dy/masa/litematica/gui/widgets/WidgetSchematicBrowser;"))
//    private WidgetSchematicBrowser useCustomWidget(int x, int y, int width, int height, GuiSchematicBrowserBase parent, ISelectionListener<WidgetFileBrowserBase.DirectoryEntry> selectionListener) {
//        return (BrowserConfig.INSTANCE.getImprovedBrowser().getBooleanValue()) ?
//                new StructureBrowserWidget(x, y, width, height, parent, selectionListener) :
//                new WidgetSchematicBrowser(x, y, width, height, parent, selectionListener);
//    }

    // Temporary until remapper constructor targets are fixed
    @Inject(method = "createListWidget(II)Lfi/dy/masa/litematica/gui/widgets/WidgetSchematicBrowser;", at = @At("HEAD"), cancellable = true)
    private void useCustomWidget(int listX, int listY, CallbackInfoReturnable<WidgetSchematicBrowser> cir) {
        if (BrowserConfig.INSTANCE.getImprovedBrowser().getBooleanValue())
            cir.setReturnValue(new StructureBrowserWidget(listX, listY, 100, 100, (GuiSchematicBrowserBase) (Object) this, this.getSelectionListener()));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (getListWidget() instanceof StructureBrowserWidget browserWidget) {
            browserWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
