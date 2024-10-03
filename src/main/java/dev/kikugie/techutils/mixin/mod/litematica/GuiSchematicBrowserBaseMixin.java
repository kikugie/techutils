package dev.kikugie.techutils.mixin.mod.litematica;

import dev.kikugie.techutils.feature.preview.gui.PreviewRenderManager;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.gui.widgets.WidgetSchematicBrowser;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Creates a {@link PreviewRenderManager} on opening schematic list and passes user actions to it.
 */
@Mixin(value = GuiSchematicBrowserBase.class, remap = false)
public abstract class GuiSchematicBrowserBaseMixin extends GuiListBase<WidgetFileBrowserBase.DirectoryEntry, WidgetDirectoryEntry, WidgetSchematicBrowser> {
	@Unique
	private PreviewRenderManager manager;

	protected GuiSchematicBrowserBaseMixin(int listX, int listY) {
		super(listX, listY);
	}

	@Override
	protected void closeGui(boolean showParent) {
		PreviewRenderManager.close();
		super.closeGui(showParent);
	}

	@SuppressWarnings("DataFlowIssue")
	@Override
	public void initGui() {
		this.manager = PreviewRenderManager.init((GuiSchematicBrowserBase) (Object) this);
		super.initGui();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		this.manager.profile().scrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		this.manager.profile().dragged(mouseX, mouseY, deltaX, deltaY, button);
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
		this.manager.profile().released(mouseX, mouseY);
		return super.mouseReleased(mouseX, mouseY, mouseButton);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		this.manager.profile().clicked(mouseX, mouseY, mouseButton);
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
