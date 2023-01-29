package me.kikugie.techutils.mixin;

import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import me.kikugie.techutils.render.litematica.LitematicRenderManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiSchematicBrowserBase.class)
public abstract class LitematicaGuiSchematicBrowserBaseMixin extends GuiListBase {
    protected LitematicaGuiSchematicBrowserBaseMixin(int listX, int listY) {
        super(listX, listY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        LitematicRenderManager.getInstance().mouseScrolled(mouseX, mouseY, amount);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        LitematicRenderManager.getInstance().mouseDragged(deltaX);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        LitematicRenderManager.getInstance().mouseReleased();
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        LitematicRenderManager.getInstance().mouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void closeGui(boolean showParent) {
        LitematicRenderManager.reset();
        super.closeGui(showParent);
    }

    @Override
    public void initGui() {
        LitematicRenderManager.Companion.init((GuiSchematicBrowserBase) (Object) this);
        super.initGui();
    }
}
