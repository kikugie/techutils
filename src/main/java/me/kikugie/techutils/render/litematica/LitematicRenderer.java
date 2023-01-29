package me.kikugie.techutils.render.litematica;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.litematica.gui.GuiSchematicBrowserBase;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.util.FileType;
import fi.dy.masa.litematica.util.WorldUtils;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LitematicRenderer {
    private static final double MAX_BLOCK_WIDTH = Math.cos(Math.PI / 6) * 2;
    private static final Color4f BAR_COLOR = new Color4f(0.25f, 1f, 0.25f, 1f);
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final int slant;
    public int angle = 135;
    private LitematicMesh mesh;
    private double horizontalSize;
    private double verticalSize;
    private int progressBarCooldown = 30;

    public LitematicRenderer(WidgetFileBrowserBase.DirectoryEntry entry, GuiSchematicBrowserBase gui, int slant) {
        this.slant = slant;
        LitematicaSchematic schematic;
        FileType fileType = FileType.fromFile(entry.getFullPath());
        switch (fileType) {
            case LITEMATICA_SCHEMATIC ->
                    schematic = LitematicaSchematic.createFromFile(entry.getDirectory(), entry.getName());
            case SCHEMATICA_SCHEMATIC ->
                    schematic = WorldUtils.convertSchematicaSchematicToLitematicaSchematic(entry.getDirectory(), entry.getName(), false, gui);
            case SPONGE_SCHEMATIC ->
                    schematic = WorldUtils.convertSpongeSchematicToLitematicaSchematic(entry.getDirectory(), entry.getName());
            case VANILLA_STRUCTURE ->
                    schematic = WorldUtils.convertStructureToLitematicaSchematic(entry.getDirectory(), entry.getName());
            default -> {
                return;
            }
        }
        this.mesh = new LitematicMesh(schematic, true);
        var shortestSide = Math.min(mesh.getSize().getX(), mesh.getSize().getZ());
        var longestSide = Math.max(mesh.getSize().getX(), mesh.getSize().getZ());
        this.horizontalSize = shortestSide * MAX_BLOCK_WIDTH + longestSide - shortestSide;
        this.verticalSize = longestSide * Math.tan(Math.toRadians(slant)) + mesh.getSize().getY();
    }

    public void render(MatrixStack matrices, int x, int y, int size) {
        if (mesh == null) return;
        RenderUtils.drawOutlinedBox(x, y, size, size, -1610612736, -6710887);
        if (mesh.isComplete()) {
            renderMesh(matrices, x, y, size);
        } else {
            renderProgressBar(x, y, size);
        }
    }

    private void renderProgressBar(int x, int y, int size) {
        if (progressBarCooldown > 0) {
            progressBarCooldown--;
            return;
        }

        int barWidth = size - 4;
        int barHeight = 8;
        int barX = x + 2;
        int barY = y + size / 2 - 4;
        int fill = (int) (mesh.getCompletion() * (barWidth - 2));

        RenderUtils.drawOutlinedBox(barX, barY, barWidth, barHeight, -1610612736, -6710887);
        RenderUtils.drawRect(barX + 1, barY + 1, fill, barHeight - 2, BAR_COLOR.intValue);
    }

    private void renderMesh(MatrixStack matrices, int x, int y, int size) {
        final Window window = MinecraftClient.getInstance().getWindow();
        final float aspectRatio = window.getFramebufferWidth() / (float) window.getFramebufferHeight();

        RenderSystem.backupProjectionMatrix();
        Matrix4f projectionMatrix = new Matrix4f().setOrtho(-aspectRatio, aspectRatio, -1, 1, -1000, 3000);
        RenderSystem.setProjectionMatrix(projectionMatrix);
        matrices.push();
        matrices.loadIdentity();

//        matrices.translate(mesh.space.fullBox.minX, mesh.space.fullBox.minY, mesh.space.fullBox.minZ);
        translateToCoords(matrices, x + size / 2, y + size / 2);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(slant));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
        float scaleFactor = getScaleFactor(size);
        matrices.scale(scaleFactor, scaleFactor, scaleFactor);
        RenderSystem.applyModelViewMatrix();
        emitVertices();
        drawModel(matrices.peek().getPositionMatrix());
        matrices.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
    }

    private void drawModel(Matrix4f viewMatrix) {
        draw(viewMatrix);
        MatrixStack meshStack = new MatrixStack();
        viewMatrix.translate((float) -mesh.space.fullBox.minX, (float) -mesh.space.fullBox.minY, (float) -mesh.space.fullBox.minZ);
        viewMatrix.translate(-mesh.getSize().getX() / 2f, -mesh.getSize().getY() / 2f, -mesh.getSize().getZ() / 2f);

        meshStack.multiplyPositionMatrix(viewMatrix);
        mesh.render(meshStack);
    }

    private void draw(Matrix4f viewMatrix) {
        final var lightTransform = new Matrix4f(viewMatrix);
        Vector4f lightDirection = new Vector4f(1F, 0.35F, 0, 0.0F);
        lightTransform.invert();
        lightDirection.mul(lightTransform);

        final var transformedLightDirection = new Vector3f(lightDirection.x, lightDirection.y, lightDirection.z);
        RenderSystem.setShaderLights(transformedLightDirection, transformedLightDirection);

        MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().draw();
    }

    private void emitVertices() {
        var matrices = new MatrixStack();
        var vertexConsumers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        matrices.loadIdentity();
        matrices.translate(-mesh.space.fullBox.minX, -mesh.space.fullBox.minY, -mesh.space.fullBox.minZ);
        matrices.translate(-mesh.getSize().getX() / 2f, -mesh.getSize().getY() / 2f, -mesh.getSize().getZ() / 2f);

        final var blockEntities = mesh.getBlockEntities();
        blockEntities.forEach((blockPos, entity) -> {
            matrices.push();
            matrices.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            client.getBlockEntityRenderDispatcher().render(entity, 0, matrices, vertexConsumers);
            matrices.pop();
        });

        draw(RenderSystem.getModelViewMatrix());
    }

    private void translateToCoords(MatrixStack matrixStack, int x, int y) {
        final Screen screen = MinecraftClient.getInstance().currentScreen;
        final int w = screen.width;
        final int h = screen.height;
        matrixStack.translate((2f * x - w) / h, -(2f * y - h) / h, 0);
    }

    private float getScaleFactor(int size) {
        return (float) ((size * 2) / (Math.max(horizontalSize, verticalSize) * MinecraftClient.getInstance().currentScreen.height));
    }
}