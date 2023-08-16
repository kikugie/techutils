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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4f;
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
    private int progressBarCooldown = 30;

    public PreviewRenderer(LitematicaSchematic schematic, InteractionProfile profile) {
        this.profile = profile;
        this.mesh = new LitematicMesh(Objects.requireNonNull(schematic,
                "Failed to load schematic: " + schematic.getFile()));
        var shortestSide = Math.min(this.mesh.size().getX(), this.mesh.size().getZ());
        var longestSide = Math.max(this.mesh.size().getX(), this.mesh.size().getZ());
        this.horizontalSize = shortestSide * MAX_BLOCK_WIDTH + longestSide - shortestSide;
        this.verticalSize = longestSide * Math.tan(profile.slant()) + this.mesh.size().getY();
    }

    public void render(DrawContext context, int x, int y, int size) {
        this.profile.set(x, y, size);
        RenderUtils.drawOutlinedBox(x, y, size, size, -1610612736, -6710887);
        if (this.mesh.complete()) {
            renderMesh(context, x, y, size);
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
        int fill = (int) (this.mesh.progress() * (barWidth - 2));

        RenderUtils.drawOutlinedBox(barX, barY, barWidth, barHeight, -1610612736, -6710887);
        RenderUtils.drawRect(barX + 1, barY + 1, fill, barHeight - 2, BAR_COLOR.intValue);
    }

    @SuppressWarnings("deprecation")
    private void renderMesh(DrawContext context, int x, int y, int size) {
        MatrixStack matrices = context.getMatrices();
        Window window = MinecraftClient.getInstance().getWindow();
        float aspectRatio = window.getFramebufferWidth() / (float) window.getFramebufferHeight();

        RenderSystem.backupProjectionMatrix();
        Matrix4f projectionMatrix = new Matrix4f().setOrtho(-aspectRatio, aspectRatio, -1, 1, -1000, 3000);
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorter.BY_Z);
        matrices.push();
        matrices.loadIdentity();
//        context.enableScissor(x + 1, y + 1, x + size - 2, y + size - 2);

        // Position
        translateToCoords(matrices, (int) (x + this.profile.dx() + size / 2), (int) (y + +this.profile.dy() + size / 2));

        // Rotation
        matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) this.profile.slant()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) this.profile.angle()));

        // Scale
        float scale = scaleFactor(size) * this.profile.scale();
        matrices.scale(scale, scale, scale);

        RenderSystem.applyModelViewMatrix();
        RenderSystem.runAsFancy(this::drawEntities);
        drawModel(matrices);
//        context.disableScissor();
        matrices.pop();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
    }

    private void drawModel(MatrixStack matrices) {
        matrices.push();
        applyLight(matrices.peek().getPositionMatrix());
        this.mesh.render(matrices);
        matrices.pop();
    }

    private void applyLight(Matrix4f viewMatrix) {
        Matrix4f lightTransform = new Matrix4f(viewMatrix);
        Vector4f lightDirection = new Vector4f(1F, 0.35F, 0, 0.0F);
        lightTransform.invert();
        lightDirection.mul(lightTransform);

        final var transformedLightDirection = new Vector3f(lightDirection.x, lightDirection.y, lightDirection.z);
        RenderSystem.setShaderLights(transformedLightDirection, transformedLightDirection);

        MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().draw();
    }

    private void drawEntities() {
        MatrixStack matrices = new MatrixStack();
        VertexConsumerProvider vertexConsumers = this.client.getBufferBuilders().getEntityVertexConsumers();

        matrices.loadIdentity();
        BlockPos min = this.mesh.getFullBox().getMin();
        Vec3i size = this.mesh.size();
        matrices.translate(-min.getX(), -min.getY(), -min.getZ());
        matrices.translate(-size.getX() / 2f, -size.getY() / 2f, -size.getZ() / 2f);

        final var blockEntities = this.mesh.blockEntities();
        blockEntities.forEach((blockPos, entity) -> {
            matrices.push();
            matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            this.client.getBlockEntityRenderDispatcher().render(entity, 0, matrices, vertexConsumers);
            matrices.pop();
        });
        applyLight(RenderSystem.getModelViewMatrix());

        final var entities = this.mesh.entities();
        entities.forEach(entry -> {
            matrices.push();
            Vec3d pos = entry.entity().getPos();

            matrices.translate(pos.x, pos.y, pos.z);
            float tickDelta = this.client.getTickDelta();
            this.client.getEntityRenderDispatcher().render(entry.entity(), pos.x, pos.y, pos.z, entry.entity().getYaw(tickDelta), tickDelta, matrices, vertexConsumers, entry.light());
            matrices.pop();
            applyLight(RenderSystem.getModelViewMatrix());
        });
    }

    private void translateToCoords(MatrixStack matrixStack, int x, int y) {
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
