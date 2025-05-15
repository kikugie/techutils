package dev.kikugie.techutils.feature.preview.interaction;

import dev.kikugie.techutils.config.LitematicConfigs;
import dev.kikugie.techutils.feature.preview.gui.PreviewRenderManager;
import net.minecraft.client.MinecraftClient;

public class SpinProfile implements InteractionProfile {
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final PreviewRenderManager manager;
	private int x;
	private int y;
	private int viewport;
	private double angle = Math.PI / 4;

	private boolean holding = false;

	SpinProfile(PreviewRenderManager manager) {
		this.manager = manager;
	}

	@Override
	public void set(int x, int y, int viewportSize) {
		this.x = x;
		this.y = y;
		this.viewport = viewportSize;

		if (this.holding)
			return;

		float delta = this.client.getTickDelta();
		this.angle = wrapOnce(this.angle + delta * LitematicConfigs.ROTATION_FACTOR.getDoubleValue() * 0.1, 360.0);
	}

	private static double wrapOnce(double a, double b) {
		return a > b ? a - b : a;
	}

	@Override
	public void scrolled(double x, double y, double horizontalAmount, double verticalAmount) {

	}

	@Override
	public void dragged(double x, double y, double dx, double dy, int button) {

	}

	@Override
	public void released(double x, double y) {
		this.holding = false;
	}

	@Override
	public void clicked(double x, double y, int button) {
		if (inViewport(x, y))
			this.holding = true;
	}

	@Override
	public boolean inViewport(double x, double y) {
		return x > this.x && y > this.y && x < this.x + this.viewport && y < this.y + this.viewport;
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
