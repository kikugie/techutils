package dev.kikugie.techutils.feature.preview.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import dev.kikugie.techutils.TechUtilsMod;
import dev.kikugie.techutils.mixin.preview.BlockEntityAccessor;
import dev.kikugie.techutils.util.ValidBox;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.EntityUtils;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LitematicMesh {
	private final LitematicaSchematic schematic;
	private final MinecraftClient client;

	private final Map<RenderLayer, VertexBuffer> bufferStorage;
	private final Map<RenderLayer, BufferBuilder> initializedLayers;

	private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
	private final List<EntityEntry> entities = new ArrayList<>();
	private final ValidBox fullBox;
	private final int totalBlocks;
	private int processed = 0;
	private boolean done = false;

	public LitematicMesh(LitematicaSchematic schematic) {
		this.schematic = schematic;
		this.client = MinecraftClient.getInstance();
		this.bufferStorage = new HashMap<>();
		this.initializedLayers = new HashMap<>();
		this.totalBlocks = schematic.getMetadata().getTotalBlocks();
		this.fullBox = fullBox(schematic.getAreas().values());

		CompletableFuture.runAsync(this::build, Util.getMainWorkerExecutor()).whenComplete((unused, throwable) -> {
			if (throwable != null) {
				throw new RuntimeException(throwable);
			}
		});
	}

	private void build() {
		var allocatorStorage = new BlockBufferAllocatorStorage();

		MatrixStack matrices = new MatrixStack();
		Random random = Random.createLocal();

		Vec3i corner = this.fullBox.getMin();
		Vec3i size = this.schematic.getTotalSize();

		Vec3d offset = new Vec3d(
			-corner.getX() - (double) size.getX() / 2d,
			-corner.getY() - (double) size.getY() / 2d,
			-corner.getZ() - (double) size.getZ() / 2d);
		matrices.translate(offset.x, offset.y, offset.z);

		try {
			CompletableFuture<List<EntityEntry>> entitiesFuture = new CompletableFuture<>();
			this.client.execute(() -> entitiesFuture.complete(readEntities()));

			this.schematic.getAreas().keySet().forEach(region -> buildRegion(matrices, region, random, allocatorStorage));

			var future = new CompletableFuture<Void>();
			RenderSystem.recordRenderCall(() -> {
				this.initializedLayers.forEach((renderLayer, bufferBuilder) -> {
					final var vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

					var built = bufferBuilder.endNullable();
					if (built == null)
						return;

					if (renderLayer == RenderLayer.getTranslucent()) {
						built.sortQuads(allocatorStorage.get(renderLayer), VertexSorter.byDistance(0, 0, 1000));
					}

					vertexBuffer.bind();
					vertexBuffer.upload(built);

					var discardedBuffer = this.bufferStorage.put(renderLayer, vertexBuffer);
					if (discardedBuffer != null) {
						discardedBuffer.close();
					}
				});

				future.complete(null);
			});
			future.join();

			entitiesFuture.join().forEach(entry -> {
				Vec3d newPos = entry.entity.getPos().add(offset);
				entry.entity().updatePosition(newPos.x, newPos.y, newPos.z);
				this.entities.add(entry);
			});

			this.entities.addAll(entitiesFuture.join());
			this.done = true;
		} catch (Exception e) {
			TechUtilsMod.LOGGER.error("Rendering gone wrong:\n", e);
		} finally {
			allocatorStorage.close();
		}
	}

	private List<EntityEntry> readEntities() {
		ArrayList<EntityEntry> entities = new ArrayList<>();
		this.schematic.getAreas().keySet().forEach(region -> {
			List<LitematicaSchematic.EntityInfo> schematicEntities = this.schematic.getEntityListForRegion(region);
			assert schematicEntities != null;

			schematicEntities.forEach(entityInfo -> {
				Entity origEntity = EntityUtils.createEntityAndPassengersFromNBT(entityInfo.nbt, this.client.world);
				var entity = origEntity.getType().create(this.client.world);
				entity.copyFrom(origEntity);
				entity.copyPositionAndRotation(origEntity);
				entity.tick();
				entities.add(new EntityEntry(
					entity,
					0xFF00FF));
			});
		});
		return entities;
	}

	private void buildRegion(MatrixStack matrices, String region, Random random, BlockBufferAllocatorStorage allocatorStorage) {
		Box box = this.schematic.getAreas().get(region);
		RegionBlockView view = new RegionBlockView(
			Objects.requireNonNull(this.schematic.getSubRegionContainer(region)),
			box);
		Map<BlockPos, NbtCompound> schematicBlockEntities = this.schematic.getBlockEntityMapForRegion(region);

		assert schematicBlockEntities != null;
		assert this.client.player != null;

		BlockRenderManager manager = this.client.getBlockRenderManager();

		for (BlockPos pos : BlockPos.iterate(view.box.getPos1(), view.box.getPos2())) {
			BlockState state = view.getBlockState(pos);
			if (state.isAir())
				continue;

			if (state.getBlock() instanceof BlockEntityProvider provider) {
				BlockEntity blockEntity = provider.createBlockEntity(this.client.getCameraEntity().getBlockPos(), state);

				if (blockEntity != null) {
					((BlockEntityAccessor) blockEntity).setCachedState(state);
					blockEntity.read(schematicBlockEntities.getOrDefault(pos, new NbtCompound()), MinecraftClient.getInstance().world.getRegistryManager());
					blockEntity.setWorld(this.client.world);
					this.blockEntities.put(pos.toImmutable(), blockEntity);
				}
			}

			if (!state.getFluidState().isEmpty()) {
				FluidState fluidState = state.getFluidState();
				RenderLayer fluidLayer = RenderLayers.getFluidLayer(fluidState);

				matrices.push();
				matrices.translate(-(pos.getX() & 15), -(pos.getY() & 15), -(pos.getZ() & 15));
				matrices.translate(pos.getX(), pos.getY(), pos.getZ());

				BufferBuilder bufferBuilder = this.getOrCreateBuffer(allocatorStorage, fluidLayer);
				manager.renderFluid(pos, view, new FluidVertexConsumer(bufferBuilder, matrices.peek().getPositionMatrix()), state, fluidState);

				matrices.pop();
			}

			matrices.push();
			matrices.translate(pos.getX(), pos.getY(), pos.getZ());

			BakedModel model = manager.getModel(state);
			RenderLayer renderLayer = RenderLayers.getBlockLayer(state);

			if (state.getRenderType() == BlockRenderType.MODEL) {
				manager.getModelRenderer().render(view, model, state, pos, matrices, getOrCreateBuffer(allocatorStorage, renderLayer), true, random, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);
			}

			matrices.pop();
			this.processed++;
		}
	}

	private BufferBuilder getOrCreateBuffer(BlockBufferAllocatorStorage allocatorStorage, RenderLayer layer) {
		return this.initializedLayers.computeIfAbsent(layer, renderLayer ->
			new BufferBuilder(allocatorStorage.get(renderLayer), VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL)
		);
	}

	public void render(MatrixStack matrices) {
		final var matrix = matrices.peek().getPositionMatrix();

		final RenderLayer translucent = RenderLayer.getTranslucent();
		this.bufferStorage.forEach((renderLayer, vertexBuffer) -> {
			if (renderLayer == translucent) return;
			draw(renderLayer, vertexBuffer, matrix);
		});

		if (this.bufferStorage.containsKey(translucent)) {
			draw(translucent, this.bufferStorage.get(translucent), matrix);
		}

		VertexBuffer.unbind();
	}

	private void draw(RenderLayer renderLayer, VertexBuffer vertexBuffer, Matrix4f matrix) {
		renderLayer.startDrawing();

		vertexBuffer.bind();
		vertexBuffer.draw(matrix, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());

		renderLayer.endDrawing();
	}

	private ValidBox fullBox(Collection<Box> boxes) {
		int[] corners = {0, 0, 0, 0, 0, 0};
		for (Box box : boxes) {
			ValidBox validBox = ValidBox.of(box);
			BlockPos min = validBox.getMin();
			BlockPos max = validBox.getMax();

			corners[0] = Math.min(corners[0], min.getX());
			corners[1] = Math.min(corners[1], min.getY());
			corners[2] = Math.min(corners[2], min.getZ());
			corners[3] = Math.max(corners[3], max.getX());
			corners[4] = Math.max(corners[4], max.getY());
			corners[5] = Math.max(corners[5], max.getZ());
		}
		return new ValidBox(corners);
	}

	public Map<BlockPos, BlockEntity> blockEntities() {
		return this.blockEntities;
	}

	public List<EntityEntry> entities() {
		return this.entities;
	}

	public Vec3i size() {
		return this.schematic.getTotalSize();
	}

	public boolean complete() {
		return this.done;
	}

	public float progress() {
		return this.processed / (float) this.totalBlocks;
	}

	public ValidBox getFullBox() {
		return this.fullBox;
	}

	public record EntityEntry(Entity entity, int light) {
	}
}
