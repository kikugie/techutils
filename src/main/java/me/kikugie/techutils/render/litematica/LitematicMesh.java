package me.kikugie.techutils.render.litematica;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import io.wispforest.worldmesher.renderers.WorldMesherBlockModelRenderer;
import io.wispforest.worldmesher.renderers.WorldMesherFluidRenderer;
import me.kikugie.techutils.mixin.BlockEntityAccessor;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.WorldMesherRenderContext;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LitematicMesh {
    public final LitematicaSchematic schematic;
    public final LitematicSpace space;
    private final Map<RenderLayer, VertexBuffer> bufferStorage;
    private final Map<RenderLayer, BufferBuilder> initializedLayers;
    private final BlockRenderManager blockRenderManager;
    private final HashMap<BlockPos, BlockEntity> blockEntities;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private float completion = 0f;
    private boolean done = false;

    public LitematicMesh(LitematicaSchematic schematic, boolean async) {
        this.schematic = schematic;
        this.space = new LitematicSpace(schematic);

        this.bufferStorage = new HashMap<>();
        this.initializedLayers = new HashMap<>();
        this.blockEntities = new HashMap<>();
        this.blockRenderManager = client.getBlockRenderManager();

        if (async) {
            CompletableFuture.runAsync(this::build, Util.getMainWorkerExecutor()).whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException(throwable);
                }
            });
        } else {
            build();
        }
    }

    public void build() {
        final var matrices = new MatrixStack();
        WorldMesherRenderContext renderContext = RendererAccess.INSTANCE.getRenderer() instanceof IndigoRenderer ? new WorldMesherRenderContext(client.world, this::getOrCreateBuffer) : null;
        final var blockRenderer = new WorldMesherBlockModelRenderer();
        final var fluidRenderer = new WorldMesherFluidRenderer();
        final var random = Random.createLocal();

        int processedBlocks = 0;

        for (String region : space.boxes.keySet()) {
            Box box = space.boxes.get(region);
            LitematicRegionBlockView blockView = space.regionBlockViewMap.get(region);
            BlockPos origin = new BlockPos((int) box.minX, (int) box.minY, (int) box.minZ);
            BlockPos end = new BlockPos((int) box.maxX, (int) box.maxY, (int) box.maxZ);

            for (BlockPos pos : BlockPos.iterate(origin, end)) {
                BlockState state = blockView.getBlockState(pos);
                if (state.isAir()) {
                    continue;
                }

                if (state.getBlock() instanceof BlockEntityProvider provider) {
                    // TODO: 19/01/2023 Add block entity data from region
                    // TODO: 21/01/2023 Bake block entities
                    BlockEntity blockEntity = provider.createBlockEntity(client.player.getBlockPos(), state);
                    if (blockEntity != null) {
                        populateBlockEntity(blockEntity, state);
                        blockEntities.put(pos.toImmutable(), blockEntity);
                    }
                }

                if (!state.getFluidState().isEmpty()) {
                    FluidState fluidState = state.getFluidState();

                    RenderLayer fluidLayer = RenderLayers.getFluidLayer(fluidState);

                    matrices.push();
                    matrices.translate(-(pos.getX() & 15), -(pos.getY() & 15), -(pos.getZ() & 15));
                    matrices.translate(pos.getX(), pos.getY(), pos.getZ());

                    fluidRenderer.setMatrix(matrices.peek().getPositionMatrix());
                    fluidRenderer.render(blockView, pos, getOrCreateBuffer(fluidLayer), state, fluidState);

                    matrices.pop();
                }

                matrices.push();
                matrices.translate(pos.getX(), pos.getY(), pos.getZ());

                BakedModel model = blockRenderManager.getModel(state);
                RenderLayer renderLayer = RenderLayers.getBlockLayer(state);

                if (!((FabricBakedModel) model).isVanillaAdapter() && renderContext != null) {
                    renderContext.tessellateBlock(blockView, state, pos, model, matrices);
                } else if (state.getRenderType() == BlockRenderType.MODEL) {
                    blockRenderer.render(blockView, model, state, pos, matrices, getOrCreateBuffer(renderLayer), true, random, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);
                }
                matrices.pop();

                processedBlocks++;
                completion = processedBlocks / (float) schematic.getMetadata().getTotalBlocks();
            }
        }

        if (initializedLayers.containsKey(RenderLayer.getTranslucent())) {
            var translucentBuilder = initializedLayers.get(RenderLayer.getTranslucent());
            translucentBuilder.sortFrom(0, 0, 1000);
        }

        var future = new CompletableFuture<Void>();
        RenderSystem.recordRenderCall(() -> {
            initializedLayers.forEach((renderLayer, bufferBuilder) -> {
                final var vertexBuffer = new VertexBuffer();

                vertexBuffer.bind();
                vertexBuffer.upload(bufferBuilder.end());

                bufferStorage.put(renderLayer, vertexBuffer);
            });

            future.complete(null);
        });
        future.join();
        done = true;
    }

    public void render(MatrixStack matrices) {
        final var matrix = matrices.peek().getPositionMatrix();

        final RenderLayer translucent = RenderLayer.getTranslucent();
        bufferStorage.forEach((renderLayer, vertexBuffer) -> {
            if (renderLayer == translucent) return;
            draw(renderLayer, vertexBuffer, matrix);
        });

        if (bufferStorage.containsKey(translucent)) {
            draw(translucent, bufferStorage.get(translucent), matrix);
        }

        VertexBuffer.unbind();
    }

    private void draw(RenderLayer renderLayer, VertexBuffer vertexBuffer, Matrix4f matrix) {
        renderLayer.startDrawing();

        vertexBuffer.bind();
        vertexBuffer.draw(matrix, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());

        renderLayer.endDrawing();
    }

    private VertexConsumer getOrCreateBuffer(RenderLayer layer) {
        if (!initializedLayers.containsKey(layer)) {
            BufferBuilder builder = new BufferBuilder(layer.getExpectedBufferSize());
            initializedLayers.put(layer, builder);
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        }
        return initializedLayers.get(layer);
    }

    private void populateBlockEntity(BlockEntity blockEntity, BlockState state) {
        ((BlockEntityAccessor) blockEntity).setCachedState(state);
        blockEntity.setWorld(client.world);
    }

    public boolean isComplete() {
        return done;
    }

    public float getCompletion() {
        return completion;
    }

    public Vec3i getSize() {
        return schematic.getTotalSize();
    }

    public HashMap<BlockPos, BlockEntity> getBlockEntities() {
        return blockEntities;
    }
}
