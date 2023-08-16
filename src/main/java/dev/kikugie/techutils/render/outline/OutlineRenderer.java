package dev.kikugie.techutils.render.outline;

import dev.kikugie.techutils.mixin.containerscan.WorldRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

import java.util.*;
import java.util.function.Predicate;

public class OutlineRenderer {
    private static final RenderLayer OUTLINE_LAYER = RenderLayer.getOutline(new Identifier("textures/misc/white.png"));
    private static final Map<BlockPos, RenderEntry> entries = new Hashtable<>();
    private static final Map<Box, Cuboid> compiledShapes = new Hashtable<>();
    public static boolean isRendering = false;

    public static void add(BlockView world, int color, Predicate<Vec3d> renderCondition, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        List<Box> boundingBoxes = new ArrayList<>(state.getOutlineShape(world, pos).getBoundingBoxes());
        if (boundingBoxes.isEmpty())
            boundingBoxes.add(new Box(pos));

        List<Cuboid> cuboids = new ArrayList<>();
        for (Box box : boundingBoxes)
            cuboids.add(compileAndCache(box));
        entries.put(pos, new RenderEntry(cuboids, pos, color, renderCondition));
    }

    public static void render(WorldRenderContext context) {
        if (!entries.isEmpty()) {
            MatrixStack matrices = context.matrixStack();
            Vec3d camera = context.camera().getPos();
            WorldRendererAccessor worldRenderer = (WorldRendererAccessor) context.worldRenderer();

            if (!isRendering) {
                worldRenderer.getEntityOutlinePostProcessor().render(context.tickDelta());
                MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
            }

            matrices.push();
            matrices.translate(-camera.x, -camera.y, -camera.z);
            renderOutlines(matrices, camera, worldRenderer.getBufferBuilders().getOutlineVertexConsumers());
            matrices.pop();
        }
        isRendering = false;
    }

    private static void renderOutlines(MatrixStack matrices, Vec3d camera, OutlineVertexConsumerProvider consumers) {
        for (RenderEntry entry : entries.values()) {
            if (!entry.renderCondition.test(camera))
                continue;

            consumers.setColor(entry.color >> 16 & 255, entry.color >> 8 & 255, entry.color & 255, entry.color >> 24 & 255);

            matrices.push();
            matrices.translate(entry.pos.getX(), entry.pos.getY(), entry.pos.getZ());
            for (Cuboid cuboid : entry.model)
                cuboid.renderCuboid(matrices.peek(), consumers.getBuffer(OUTLINE_LAYER), 0, OverlayTexture.field_32955, 1, 1, 1, 1);
            matrices.pop();
        }
    }

    private static Cuboid compileAndCache(Box box) {
        if (compiledShapes.containsKey(box))
            return compiledShapes.get(box);

        float sizeX = (float) (box.maxX - box.minX);
        float sizeY = (float) (box.maxY - box.minY);
        float sizeZ = (float) (box.maxZ - box.minZ);
        Cuboid cuboid = new Cuboid(0, 0, (float) box.minX * 16, (float) box.minY * 16, (float) box.minZ * 16, sizeX * 16, sizeY * 16, sizeZ * 16, 0, 0, 0, false, 0, 0, Set.of(Direction.values()));
        compiledShapes.put(box, cuboid);
        return cuboid;
    }

    private record RenderEntry(Collection<Cuboid> model, BlockPos pos, int color,
                               Predicate<Vec3d> renderCondition) {
    }
}
