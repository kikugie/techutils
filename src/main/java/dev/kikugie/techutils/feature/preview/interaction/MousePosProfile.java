package dev.kikugie.techutils.feature.preview.interaction;

import dev.kikugie.techutils.config.LitematicConfigs;
import dev.kikugie.techutils.feature.preview.gui.PreviewRenderManager;
import net.minecraft.client.MinecraftClient;

public class MousePosProfile implements InteractionProfile {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final PreviewRenderManager manager;
    private int x;
    private int y;
    private int viewport;
    private double angle = Math.PI / 4;

    public MousePosProfile(PreviewRenderManager manager) {
        this.manager = manager;
    }

    @Override
    public void set(int x, int y, int viewportSize) {
        this.x = x;
        this.y = y;
        this.viewport = viewportSize;

        double mouseX = this.client.mouse.getX();
        int windowWidth = this.client.getWindow().getFramebufferWidth();
        this.angle = mouseX / windowWidth * Math.PI * 2 * LitematicConfigs.ROTATION_FACTOR.getDoubleValue();
    }

    @Override
    public void scrolled(double x, double y, double amount) {

    }

    @Override
    public void dragged(double x, double y, double dx, double dy, int button) {

    }

    @Override
    public void released(double x, double y) {

    }

    @Override
    public void clicked(double x, double y, int button) {

    }

    @Override
    public boolean inViewport(double x, double y) {
        return false;
    }

    @Override
    public int x() {
        return this.x;
    }

    @Override
    public int y() {
        return this.y;
    }

    @Override
    public int viewport() {
        return this.viewport;
    }

    @Override
    public double angle() {
        return this.angle;
    }

    @Override
    public float dx() {
        return 0;
    }

    @Override
    public float dy() {
        return 0;
    }

    @Override
    public float scale() {
        return 1;
    }
}
