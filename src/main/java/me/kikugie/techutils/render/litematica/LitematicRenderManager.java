package me.kikugie.techutils.render.litematica;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import me.kikugie.techutils.config.Configs;
import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;

public class LitematicRenderManager {
    private static LitematicRenderManager instance;
    private final Map<WidgetFileBrowserBase.DirectoryEntry, LitematicRenderer> rendererCache = new HashMap<>();
    private final GuiSchematicBrowserBase gui;
    private final int slant;
    private LitematicRenderer currentRenderer;
    private int renderX;
    private int renderY;
    private int viewportSize;
    private boolean validDragging = false;

    private LitematicRenderManager(GuiSchematicBrowserBase gui) {
        this.gui = gui;
        this.slant = Configs.LitematicConfigs.RENDER_SLANT.getIntegerValue();
    }

    public static LitematicRenderManager init(GuiSchematicBrowserBase gui) {
        instance = new LitematicRenderManager(gui);
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public static LitematicRenderManager getInstance() {
        return instance;
    }

    public void setCurrentRenderer(WidgetFileBrowserBase.DirectoryEntry entry) {
        if (this.gui == null) return;
        if (rendererCache.containsKey(entry)) {
            this.currentRenderer = rendererCache.get(entry);
        } else {
            var renderer = new LitematicRenderer(entry, this.gui, this.slant);
            rendererCache.put(entry, renderer);
            this.currentRenderer = renderer;
        }
    }

    public void renderCurrent(int x, int y, int viewportSize) {
        if (currentRenderer == null) return;
        this.renderX = x;
        this.renderY = y;
        this.viewportSize = viewportSize;

        if (Configs.LitematicConfigs.RENDER_ROTATION_MODE.getStringValue().equals(RotationMode.MOUSE_POS.mode)) {
            double mouseX = MinecraftClient.getInstance().mouse.getX();
            int windowWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
            currentRenderer.angle = (int) (mouseX / windowWidth * Configs.LitematicConfigs.ROTATION_FACTOR.getDoubleValue() * 360);
        } else if (Configs.LitematicConfigs.RENDER_ROTATION_MODE.getStringValue().equals(RotationMode.FREE_SPIN.mode)) {
            long tick = MinecraftClient.getInstance().world.getTime();
            currentRenderer.angle = (int) (tick * Configs.LitematicConfigs.ROTATION_FACTOR.getDoubleValue() * 2 % 360);
        }
        this.currentRenderer.render(RenderSystem.getModelViewStack(), x, y, viewportSize);

    }

    public void mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!Configs.LitematicConfigs.RENDER_ROTATION_MODE.getStringValue().equals(RotationMode.SCROLL.mode) || !isInViewPort(mouseX, mouseY))
            return;
        currentRenderer.angle += amount * Configs.LitematicConfigs.ROTATION_FACTOR.getDoubleValue() * 10;
    }

    public void mouseDragged(double deltaX) {
        if (!Configs.LitematicConfigs.RENDER_ROTATION_MODE.getStringValue().equals(RotationMode.DRAG.mode) || !validDragging)
            return;
        currentRenderer.angle += deltaX * Configs.LitematicConfigs.ROTATION_FACTOR.getDoubleValue();
    }

    public void mouseReleased() {
        this.validDragging = false;
    }

    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (isInViewPort(mouseX, mouseY) && mouseButton == 0) {
            this.validDragging = true;
        }
    }

    private boolean isInViewPort(double mouseX, double mouseY) {
        return mouseX > renderX && mouseY > renderY && mouseX < renderX + viewportSize && mouseY < renderY + viewportSize;
    }

    public enum RotationMode implements IConfigOptionListEntry {
        MOUSE_POS("Mouse position"),
        DRAG("Drag"),
        SCROLL("Scroll"),
        FREE_SPIN("Free spin");

        private final String mode;

        RotationMode(String mode) {
            this.mode = mode;
        }

        @Override
        public String getStringValue() {
            return mode;
        }

        @Override
        public String getDisplayName() {
            return mode;
        }

        @Override
        public IConfigOptionListEntry cycle(boolean forward) {
            int mod = forward ? 1 : -1;
            return values()[(this.ordinal() + mod) % values().length];
        }

        @Override
        public IConfigOptionListEntry fromString(String value) {
            RotationMode[] vals = values();

            for (RotationMode temp : vals) {
                if (temp.mode.equalsIgnoreCase(value)) {
                    return temp;
                }
            }

            return RotationMode.DRAG;
        }
    }
}
