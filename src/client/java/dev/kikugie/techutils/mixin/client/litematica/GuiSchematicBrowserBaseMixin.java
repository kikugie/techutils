package dev.kikugie.techutils.mixin.client.litematica;

import dev.kikugie.techutils.config.BrowserConfig;
import dev.kikugie.techutils.gui.browser.StructureBrowser;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stores the preview render manager in the schematic browser. Previews are reset when the browser is closed.
 */
@Mixin(value = GuiSchematicBrowserBase.class, remap = false)
public abstract class GuiSchematicBrowserBaseMixin extends GuiListBase<WidgetFileBrowserBase.DirectoryEntry, WidgetDirectoryEntry, WidgetSchematicBrowser> {

    protected GuiSchematicBrowserBaseMixin(int listX, int listY) {
        super(listX, listY);
    }

    @Redirect(method = "createListWidget(II)Lfi/dy/masa/litematica/gui/widgets/WidgetSchematicBrowser;", at = @At(value = "NEW", target = "(IIIILfi/dy/masa/litematica/gui/GuiSchematicBrowserBase;Lfi/dy/masa/malilib/gui/interfaces/ISelectionListener;)Lfi/dy/masa/litematica/gui/widgets/WidgetSchematicBrowser;"))
    private WidgetSchematicBrowser useCustomWidget(int x, int y, int width, int height, GuiSchematicBrowserBase parent, ISelectionListener<WidgetFileBrowserBase.DirectoryEntry> selectionListener) {
        return (BrowserConfig.INSTANCE.getImprovedBrowser().getBooleanValue())
                ? new StructureBrowser(x, y, width, height, parent, selectionListener)
                : new WidgetSchematicBrowser(x, y, width, height, parent, selectionListener);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (getListWidget() instanceof StructureBrowser browserWidget) {
            browserWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
