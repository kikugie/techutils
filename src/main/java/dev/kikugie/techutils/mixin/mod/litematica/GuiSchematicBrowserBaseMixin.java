package dev.kikugie.techutils.mixin.mod.litematica;

import dev.kikugie.techutils.client.feature.preview.PreviewConfig;
import dev.kikugie.techutils.client.feature.preview.render.PreviewManager;
import dev.kikugie.techutils.client.feature.litegui.browser.StructureBrowserWidget;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stores the preview render manager in the schematic browser. Previews are reset when the browser is closed.
 */
@Restriction(require = @Condition("isometric-renders"))
@Mixin(value = GuiSchematicBrowserBase.class, remap = false)
public abstract class GuiSchematicBrowserBaseMixin extends GuiListBase<WidgetFileBrowserBase.DirectoryEntry, WidgetDirectoryEntry, WidgetSchematicBrowser> implements PreviewManager.Accessor {
    @Unique
    private final PreviewManager previewManager = new PreviewManager();

    protected GuiSchematicBrowserBaseMixin(int listX, int listY) {
        super(listX, listY);
    }

    @Redirect(method = "createListWidget(II)Lfi/dy/masa/litematica/gui/widgets/WidgetSchematicBrowser;", at = @At(value = "NEW", target = "(IIIILfi/dy/masa/litematica/gui/GuiSchematicBrowserBase;Lfi/dy/masa/malilib/gui/interfaces/ISelectionListener;)Lfi/dy/masa/litematica/gui/widgets/WidgetSchematicBrowser;"))
    private WidgetSchematicBrowser useCustomWidget(int x, int y, int width, int height, GuiSchematicBrowserBase parent, ISelectionListener<WidgetFileBrowserBase.DirectoryEntry> selectionListener) {
        return (PreviewConfig.customMetadata.getBooleanValue()) ?
                new StructureBrowserWidget(x, y, width, height, parent, selectionListener) :
                new WidgetSchematicBrowser(x, y, width, height, parent, selectionListener);
    }

    @NotNull
    @Override
    public PreviewManager getPreviewManager() {
        return this.previewManager;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.previewManager.onScroll(mouseX, mouseY, verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.previewManager.onClick(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        this.previewManager.onRelease(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.previewManager.onDrag(mouseX, mouseY, deltaX, deltaY, button);
        if (getListWidget() instanceof StructureBrowserWidget browserWidget) {
            browserWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
