package dev.kikugie.techutils.feature.preview.model;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import org.joml.Matrix4f;

public class PreviewFluidRenderer extends FluidRenderer {

	private final Matrix4f matrix;

	public PreviewFluidRenderer(Matrix4f matrix) {
		this.matrix = matrix;
		onResourceReload();
	}

	@Override
	protected void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light) {
		vertexConsumer.vertex(this.matrix, (float) x, (float) y, (float) z).color(red, green, blue, 1.0F).texture(u, v).light(light).normal(0.0F, 1.0F, 0.0F).next();
	}
}
