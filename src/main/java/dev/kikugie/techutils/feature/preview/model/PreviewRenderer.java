package dev.kikugie.techutils.feature.preview.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import dev.kikugie.techutils.feature.preview.interaction.InteractionProfile;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Objects;

public class PreviewRenderer {
	private static final double MAX_BLOCK_WIDTH = Math.cos(Math.PI / 6) * 2;
	private static final Color4f BAR_COLOR = new Color4f(0.25f, 1f, 0.25f, 1f);
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final LitematicMesh mesh;
	private final InteractionProfile profile;

	private final double horizontalSize;
	private final double verticalSize;
	private final int xSize;
	private final int ySize;
	private final int zSize;
	private int progressBarCooldown = 30;

	public PreviewRenderer(LitematicaSchematic schematic, InteractionProfile profile) {
		this.profile = profile;
		this.mesh = new LitematicMesh(Objects.requireNonNull(schematic,
			"Failed to load schematic: " + schematic.getFile()));
		final var dimensions = mesh.dimensions();
		this.xSize = (int) dimensions.getLengthX();
		this.ySize = (int) dimensions.getLengthY();
		this.zSize = (int) dimensions.getLengthZ();
		var shortestSide = Math.min(xSize, zSize);
		var longestSide = Math.max(xSize, zSize);
		this.horizontalSize = shortestSide * MAX_BLOCK_WIDTH + longestSide - shortestSide;
		this.verticalSize = longestSide * Math.tan(profile.slant()) + ySize;
	}

	public void render(DrawContext context, int x, int y, int size) {
		this.profile.set(x, y, size);
		RenderUtils.drawOutlinedBox(x, y, size, size, -1610612736, -6710887);
		if (this.mesh.canRender()) {
			drawIntoActiveFramebuffer(context, x, y, size);
		} else {
			renderProgressBar(x, y, size);
		}
	}

	private void renderProgressBar(int x, int y, int size) {
		if (this.progressBarCooldown > 0) {
			this.progressBarCooldown--;
			return;
		}

		int barWidth = size - 4;
		int barHeight = 8;
		int barX = x + 2;
		int barY = y + size / 2 - 4;
		int fill = (int) (this.mesh.buildProgress() * (barWidth - 2));

		RenderUtils.drawOutlinedBox(barX, barY, barWidth, barHeight, -1610612736, -6710887);
		RenderUtils.drawRect(barX + 1, barY + 1, fill, barHeight - 2, BAR_COLOR.intValue);
	}

	private void emitVertices(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers) {
		if (!mesh.canRender()) {
			if (mesh.state() == LitematicMesh.MeshState.CORRUPT) return;

			mesh.scheduleRebuild();
			return;
		}

		matrices.loadIdentity();
		matrices.translate(-xSize / 2f, -ySize / 2f, -zSize / 2f);

		final var blockEntities = mesh.renderInfo().blockEntities();
		blockEntities.forEach((blockPos, entity) -> {
			matrices.push();
			matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			client.getBlockEntityRenderDispatcher().render(entity, 0, matrices, vertexConsumers);
			matrices.pop();
		});

		applyLight(RenderSystem.getModelViewMatrix());

		final var entities = mesh.renderInfo().entities();
		entities.forEach((vec3d, entry) -> {
			client.getEntityRenderDispatcher().render(entry.entity(), vec3d.x, vec3d.y, vec3d.z, entry.entity().getYaw(0), 0, matrices, vertexConsumers, entry.light());
			applyLight(RenderSystem.getModelViewMatrix());
		});
	}

	private void drawIntoActiveFramebuffer(DrawContext context, int x, int y, int size) {
		Window window = client.getWindow();
		float aspectRatio = window.getFramebufferWidth() / (float) window.getFramebufferHeight();

		RenderSystem.backupProjectionMatrix();
		Matrix4f projectionMatrix = new Matrix4f().setOrtho(-aspectRatio, aspectRatio, -1, 1, -1000, 3000);
		RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorter.BY_Z);

		// Prepare model view matrix
		final var modelViewStack = RenderSystem.getModelViewStack();
		modelViewStack.pushMatrix();
		modelViewStack.identity();

		context.enableScissor(x + 1, y + 1, x + size - 2, y + size - 2);

		// Position
		translateToCoords(modelViewStack, (int) (x + this.profile.dx() + size / 2), (int) (y + this.profile.dy() + size / 2));

		// Rotation
		modelViewStack.rotate(RotationAxis.POSITIVE_X.rotation((float) this.profile.slant()));
		modelViewStack.rotate(RotationAxis.POSITIVE_Y.rotation((float) this.profile.angle()));

		// Scale
		float scale = scaleFactor(size) * this.profile.scale();
		modelViewStack.scale(scale, scale, scale);

		RenderSystem.applyModelViewMatrix();

		RenderSystem.runAsFancy(() -> {
			// Emit untransformed vertices
			this.emitVertices(
				new MatrixStack(),
				MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers()
			);

			// --> Draw
			this.draw(modelViewStack);
		});

		context.disableScissor();
		modelViewStack.popMatrix();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.restoreProjectionMatrix();
	}

	public void draw(Matrix4f modelViewMatrix) {
		if (!mesh.canRender()) return;

		applyLight(modelViewMatrix);

		final var meshStack = new MatrixStack();
		meshStack.multiplyPositionMatrix(modelViewMatrix);
		meshStack.translate(-xSize / 2f, -ySize / 2f, -zSize / 2f);
		this.mesh.render(meshStack);
	}

	private void applyLight(Matrix4f viewMatrix) {
		Matrix4f lightTransform = new Matrix4f(viewMatrix);
		Vector4f lightDirection = new Vector4f(0, 0.35F, 0.25F, 0);
		lightTransform.invert();
		lightDirection.mul(lightTransform);

		final var transformedLightDirection = new Vector3f(lightDirection.x, lightDirection.y, lightDirection.z);
		RenderSystem.setShaderLights(transformedLightDirection, transformedLightDirection);

		client.getBufferBuilders().getEntityVertexConsumers().draw();
	}

	private void translateToCoords(Matrix4fStack matrixStack, int x, int y) {
		final Screen screen = this.client.currentScreen;
		assert screen != null;
		final int w = screen.width;
		final int h = screen.height;
		matrixStack.translate((2f * x - w) / h, -(2f * y - h) / h, 0);
	}

	private float scaleFactor(int size) {
		assert this.client.currentScreen != null;
		return (float) ((size * 2) / (Math.max(this.horizontalSize, this.verticalSize) * this.client.currentScreen.height));
	}

	private int scale(int val) {
		return (int) (val * this.client.getWindow().getScaleFactor());
	}
}
