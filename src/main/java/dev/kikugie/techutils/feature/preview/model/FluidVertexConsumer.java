package dev.kikugie.techutils.feature.preview.model;

import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

public class FluidVertexConsumer implements VertexConsumer {

	private final VertexConsumer delegate;
	private final Matrix4f transform;

	public FluidVertexConsumer(VertexConsumer delegate, Matrix4f transform) {
		this.delegate = delegate;
		this.transform = transform;
	}

	@Override
	public VertexConsumer vertex(float x, float y, float z) {
		this.delegate.vertex(this.transform, x, y, z);
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		this.delegate.color(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer texture(float u, float v) {
		this.delegate.texture(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlay(int u, int v) {
		this.delegate.overlay(u, v);
		return this;
	}

	@Override
	public VertexConsumer light(int u, int v) {
		this.delegate.light(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		this.delegate.normal(x, y, z);
		return this;
	}
}
