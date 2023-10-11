package dev.kikugie.techutils.mixin.mod.litematica;

import dev.kikugie.techutils.client.feature.preview.render.PreviewInvoker;
import dev.kikugie.techutils.client.feature.preview.render.PreviewManager;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Stores the preview render manager in the schematic browser. Previews are reset when the browser is closed.
 */
@Restriction(require = @Condition("isometric-renders"))
@Mixin(value = GuiSchematicBrowserBase.class, remap = false)
public abstract class GuiSchematicBrowserBaseMixin extends GuiListBase<WidgetFileBrowserBase.DirectoryEntry, WidgetDirectoryEntry, WidgetSchematicBrowser> implements PreviewInvoker {
    @Unique
    private final PreviewManager previewManager = new PreviewManager();

    protected GuiSchematicBrowserBaseMixin(int listX, int listY) {
        super(listX, listY);
    }

    @Unique
    public void drawPreview(WidgetFileBrowserBase.DirectoryEntry entry, @NotNull DrawContext context, int x, int y, int size) {
        this.previewManager.drawPreview(entry, context, x, y, size);
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
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
