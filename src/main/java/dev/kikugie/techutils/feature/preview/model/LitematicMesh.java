package dev.kikugie.techutils.feature.preview.model;

import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import dev.kikugie.techutils.Reference;
import dev.kikugie.techutils.mixin.preview.BlockEntityAccessor;
import dev.kikugie.techutils.util.ValidBox;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.util.EntityUtils;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.WorldMesherRenderContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class LitematicMesh {
	private static final Logger LOGGER = LoggerFactory.getLogger(LitematicMesh.class);

	// Render setup data
	private final LitematicaSchematic schematic;
	private final MinecraftClient client;
	private final DummyWorld dummyWorld;
	private final BlockPos from, to;
	private final Box dimensions;

	private final boolean cull;

	private final TriFunction<PlayerEntity, BlockPos, BlockPos, List<Entity>> entitySupplier;
	private DynamicRenderInfo renderInfo = DynamicRenderInfo.EMPTY;

	// Build process data
	private MeshState state = MeshState.NEW;

	private float buildProgress = 0;
	private @Nullable CompletableFuture<Void> buildFuture = null;

	// Vertex storage
	private final Map<RenderLayer, VertexBuffer> bufferStorage = new HashMap<>();

	public LitematicMesh(LitematicaSchematic schematic) {
		this.schematic = schematic;
		this.client = MinecraftClient.getInstance();
		this.dummyWorld = DummyWorld.fromWorld(this.client.world);
		final var fullBox = fullBox(schematic.getAreas().values());

		this.from = fullBox.getMin();
		this.to = fullBox.getMax();

		this.cull = true;
		this.dimensions = Box.enclosing(this.from, this.to);
		this.entitySupplier = (playerEntity, blockPos, blockPos2) -> readEntities();

//		this.renderStartAction = renderStartAction;
//		this.renderEndAction = renderEndAction;

		this.scheduleRebuild();
	}

	private List<Entity> readEntities() {
		ArrayList<Entity> entities = new ArrayList<>();
		this.schematic.getAreas().keySet().forEach(region -> {
			List<LitematicaSchematic.EntityInfo> schematicEntities = this.schematic.getEntityListForRegion(region);
			var offset = this.schematic.getSubRegionPosition(region);
			assert schematicEntities != null;

			schematicEntities.forEach(entityInfo -> {
				var entity = EntityUtils.createEntityAndPassengersFromNBT(entityInfo.nbt, this.dummyWorld);
				entity.setPosition(
					entity.getPos()
						.add(Math.max(offset.getX(), 0), Math.max(offset.getY(), 0), Math.max(offset.getZ(), 0)));
				entities.add(entity);
			});
		});
		return entities;
	}

	private ValidBox fullBox(Collection<fi.dy.masa.litematica.selection.Box> boxes) {
		int[] corners = {0, 0, 0, 0, 0, 0};
		for (fi.dy.masa.litematica.selection.Box box : boxes) {
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

	/**
	 * Renders this world mesh into the current framebuffer, translated using the given matrix
	 *
	 * @param matrices The translation matrices. This is applied to the entire mesh
	 */
	public void render(MatrixStack matrices) {
		if (!this.canRender()) {
			throw new IllegalStateException("World mesh not prepared!");
		}

		var matrix = matrices.peek().getPositionMatrix();
		var translucent = RenderLayer.getTranslucent();

		this.bufferStorage.forEach((renderLayer, vertexBuffer) -> {
			if (renderLayer == translucent) return;
			this.drawBuffer(vertexBuffer, renderLayer, matrix);
		});

		if (this.bufferStorage.containsKey(translucent)) {
			this.drawBuffer(bufferStorage.get(translucent), translucent, matrix);
		}

		VertexBuffer.unbind();
	}

	private void drawBuffer(VertexBuffer vertexBuffer, RenderLayer renderLayer, Matrix4f matrix) {
		renderLayer.startDrawing();
//		renderStartAction.run();

		vertexBuffer.bind();
		vertexBuffer.draw(matrix, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());

//		renderEndAction.run();
		renderLayer.endDrawing();
	}

	/**
	 * Checks whether this mesh is ready for rendering
	 */
	public boolean canRender() {
		return this.state.canRender;
	}

	/**
	 * Returns the current state of this mesh, used to indicate building progress and rendering availability
	 *
	 * @return The current {@code MeshState} constant
	 */
	public MeshState state() {
		return this.state;
	}

	/**
	 * Renamed to {@link #state()}
	 */
	@Deprecated(forRemoval = true)
	public MeshState getState() {
		return this.state();
	}

	/**
	 * How much of this mesh is built
	 *
	 * @return The build progress of this mesh
	 */
	public float buildProgress() {
		return this.buildProgress;
	}

	/**
	 * Renamed to {@link #buildProgress()}
	 */
	@Deprecated(forRemoval = true)
	public float getBuildProgress() {
		return this.buildProgress();
	}

	/**
	 * @return An object describing the entities and block
	 * entities in the area this mesh is covering, with positions
	 * relative to the mesh
	 */
	public DynamicRenderInfo renderInfo() {
		return this.renderInfo;
	}

	/**
	 * Renamed to {@link #renderInfo()}
	 */
	@Deprecated(forRemoval = true)
	public DynamicRenderInfo getRenderInfo() {
		return this.renderInfo();
	}

	/**
	 * @return The origin position of this mesh's area
	 */
	public BlockPos startPos() {
		return this.from;
	}

	/**
	 * @return The end position of this mesh's area
	 */
	public BlockPos endPos() {
		return this.to;
	}

	/**
	 * @return The dimensions of this mesh's entire area
	 */
	public Box dimensions() {
		return dimensions;
	}

	/**
	 * Reset this mesh to {@link MeshState#NEW}, releasing
	 * all vertex buffers in the process
	 */
	public void reset() {
		this.bufferStorage.forEach((renderLayer, vertexBuffer) -> vertexBuffer.close());
		this.bufferStorage.clear();

		this.state = MeshState.NEW;
	}

	/**
	 * Renamed to {@link #reset()}
	 */
	@Deprecated(forRemoval = true)
	public void clear() {
		this.reset();
	}

	/**
	 * Schedule a rebuild of this mesh on
	 * the main worker executor
	 */
	public synchronized void scheduleRebuild() {
		this.scheduleRebuild(Util.getMainWorkerExecutor());
	}

	/**
	 * Schedule a rebuild of this mesh,
	 * on the supplied executor
	 *
	 * @return A future completing when the build process is finished,
	 * or {@code null} if this mesh is already building
	 */
	public synchronized CompletableFuture<Void> scheduleRebuild(Executor executor) {
		if (this.buildFuture != null) return this.buildFuture;

		this.buildProgress = 0;
		this.state = this.state != MeshState.NEW
			? MeshState.REBUILDING
			: MeshState.BUILDING;

		this.buildFuture = CompletableFuture.runAsync(this::build, executor).whenComplete((unused, throwable) -> {
			this.buildFuture = null;

			if (throwable == null) {
				state = MeshState.READY;
			} else {
				LOGGER.warn("World mesh building failed", throwable);
				state = MeshState.CORRUPT;
			}
		});

		return this.buildFuture;
	}

	private void build() {
		var allocatorStorage = new BlockBufferAllocatorStorage();

		var blockRenderManager = this.client.getBlockRenderManager();

		var matrices = new MatrixStack();
		var builderStorage = new HashMap<RenderLayer, BufferBuilder>();
		var random = Random.createLocal();

		WorldMesherRenderContext renderContext = null;
		try {
			//noinspection UnstableApiUsage
			renderContext = RendererAccess.INSTANCE.getRenderer() instanceof IndigoRenderer
				? new WorldMesherRenderContext(this.dummyWorld, layer -> this.getOrCreateBuilder(allocatorStorage, builderStorage, layer))
				: null;
		} catch (Throwable throwable) {
			var fabricApiVersion = FabricLoader.getInstance().getModContainer(Reference.MOD_ID).get().getMetadata().getCustomValue("fabric_api_build_version").getAsString();
			LOGGER.error(
				"Could not create a context for rendering Fabric API models. This is most likely due to an incompatible Fabric API version - this build of {} was compiled against '{}', try that instead",
				Reference.MOD_NAME,
				fabricApiVersion,
				throwable
			);
		}

		var entitiesFuture = new CompletableFuture<List<DynamicRenderInfo.EntityEntry>>();
		this.client.execute(() ->
			entitiesFuture.complete(this.entitySupplier.apply(this.client.player, this.from, this.to.add(1, 1, 1))
				.stream()
				.map(entity -> {
					entity.tick();
					return new DynamicRenderInfo.EntityEntry(
						entity,
						this.client.getEntityRenderDispatcher().getLight(entity, 0)
					);
				}).toList()
			)
		);

		var blockEntities = new HashMap<BlockPos, BlockEntity>();

		WorldMesherRenderContext finalRenderContext = renderContext;
		this.schematic.getAreas().keySet().forEach(region -> buildRegion(region, blockEntities, matrices, blockRenderManager, allocatorStorage, builderStorage, finalRenderContext, random));

		var future = new CompletableFuture<Void>();
		RenderSystem.recordRenderCall(() -> {
			this.bufferStorage.forEach((renderLayer, vertexBuffer) -> vertexBuffer.close());
			this.bufferStorage.clear();

			builderStorage.forEach((renderLayer, bufferBuilder) -> {
				var newBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

				var built = bufferBuilder.endNullable();
				if (built == null)
					return;

				if (renderLayer == RenderLayer.getTranslucent()) {
					built.sortQuads(allocatorStorage.get(renderLayer), VertexSorter.byDistance(0, 0, 1000));
				}

				newBuffer.bind();
				newBuffer.upload(built);

				var discardedBuffer = this.bufferStorage.put(renderLayer, newBuffer);
				if (discardedBuffer != null) {
					discardedBuffer.close();
				}
			});

			future.complete(null);
		});
		future.join();

		var entities = HashMultimap.<Vec3d, DynamicRenderInfo.EntityEntry>create();
		for (var entityEntry : entitiesFuture.join()) {
			entities.put(
				entityEntry.entity().getPos()/*.subtract(this.from.getX(), this.from.getY(), this.from.getZ())*/,
				entityEntry
			);
		}

		allocatorStorage.close();
		this.renderInfo = new DynamicRenderInfo(
			blockEntities, entities
		);
	}

	private void buildRegion(String region, HashMap<BlockPos, BlockEntity> blockEntities, MatrixStack matrices, BlockRenderManager blockRenderManager, BlockBufferAllocatorStorage allocatorStorage, HashMap<RenderLayer, BufferBuilder> builderStorage, WorldMesherRenderContext renderContext, Random random) {
		fi.dy.masa.litematica.selection.Box box = this.schematic.getAreas().get(region);
		RegionBlockView view = new RegionBlockView(
			Objects.requireNonNull(this.schematic.getSubRegionContainer(region)),
			box);
		Map<BlockPos, NbtCompound> schematicBlockEntities = this.schematic.getBlockEntityMapForRegion(region);

		int currentBlockIndex = 0;
		int blocksToBuild = (this.to.getX() - this.from.getX() + 1)
			* (this.to.getY() - this.from.getY() + 1)
			* (this.to.getZ() - this.from.getZ() + 1);

		for (var pos : BlockPos.iterate(this.from, this.to)) {
			currentBlockIndex++;
			this.buildProgress = currentBlockIndex / (float) blocksToBuild;

			var state = view.getBlockState(pos);
			if (state.isAir()) continue;

			var renderPos = pos.subtract(from);
			if (state.getBlock() instanceof BlockEntityProvider provider) {
				BlockEntity blockEntity = provider.createBlockEntity(this.client.getCameraEntity().getBlockPos(), state);

				if (blockEntity != null) {
					((BlockEntityAccessor) blockEntity).setCachedState(state);
					blockEntity.read(schematicBlockEntities.getOrDefault(pos, new NbtCompound()), this.dummyWorld.getRegistryManager());
					blockEntity.setWorld(this.dummyWorld);
					blockEntities.put(renderPos, blockEntity);
				}
			}

			if (!state.getFluidState().isEmpty()) {
				var fluidState = state.getFluidState();
				var fluidLayer = RenderLayers.getFluidLayer(fluidState);

				matrices.push();
				matrices.translate(-(pos.getX() & 15), -(pos.getY() & 15), -(pos.getZ() & 15));
				matrices.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());

				blockRenderManager.renderFluid(pos, view, new FluidVertexConsumer(this.getOrCreateBuilder(allocatorStorage, builderStorage, fluidLayer), matrices.peek().getPositionMatrix()), state, fluidState);

				matrices.pop();
			}

			matrices.push();
			matrices.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());

			var blockLayer = RenderLayers.getBlockLayer(state);

			final var model = blockRenderManager.getModel(state);
			if (renderContext != null && !model.isVanillaAdapter()) {
				renderContext.tessellateBlock(view, state, pos, model, matrices);
			} else if (state.getRenderType() == BlockRenderType.MODEL) {
				blockRenderManager.getModelRenderer().render(view, model, state, pos, matrices, this.getOrCreateBuilder(allocatorStorage, builderStorage, blockLayer), cull, random, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);
			}

			matrices.pop();
		}
	}

	private VertexConsumer getOrCreateBuilder(BlockBufferAllocatorStorage allocatorStorage, Map<RenderLayer, BufferBuilder> builderStorage, RenderLayer layer) {
		return builderStorage.computeIfAbsent(layer, renderLayer ->
			new BufferBuilder(allocatorStorage.get(layer),  VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL)
		);
	}

	public static class Builder {

		private final BlockRenderView world;
		private final TriFunction<PlayerEntity, BlockPos, BlockPos, List<Entity>> entitySupplier;

		private final BlockPos origin;
		private final BlockPos end;
		private boolean cull = true;
		private boolean useGlobalNeighbors = false;
		private boolean freezeEntities = false;

		private Runnable startAction = () -> {
		};
		private Runnable endAction = () -> {
		};

		@Deprecated(forRemoval = true)
		public Builder(BlockRenderView world, BlockPos origin, BlockPos end, Function<PlayerEntity, List<Entity>> entitySupplier) {
			this.world = world;
			this.origin = origin;
			this.end = end;
			this.entitySupplier = (player, $, $$) -> entitySupplier.apply(player);
		}

		public Builder(BlockRenderView world, BlockPos origin, BlockPos end, TriFunction<PlayerEntity, BlockPos, BlockPos, List<Entity>> entitySupplier) {
			this.world = world;
			this.origin = origin;
			this.end = end;
			this.entitySupplier = entitySupplier;
		}

		public Builder(World world, BlockPos origin, BlockPos end) {
			this(world, origin, end, (except, min, max) -> world.getOtherEntities(except, Box.enclosing(min, max), entity -> !(entity instanceof PlayerEntity)));
		}

		public Builder(BlockRenderView world, BlockPos origin, BlockPos end) {
			this(world, origin, end, (except) -> List.of());
		}

		public Builder disableCulling() {
			this.cull = false;
			return this;
		}

		public Builder useGlobalNeighbors() {
			this.useGlobalNeighbors = true;
			return this;
		}

		public Builder freezeEntities() {
			this.freezeEntities = true;
			return this;
		}

		public Builder renderActions(Runnable startAction, Runnable endAction) {
			this.startAction = startAction;
			this.endAction = endAction;
			return this;
		}
	}

	public enum MeshState {
		NEW(false, false),
		BUILDING(true, false),
		REBUILDING(true, true),
		READY(false, true),
		CORRUPT(false, false);

		public final boolean isBuildStage;
		public final boolean canRender;

		MeshState(boolean buildStage, boolean canRender) {
			this.isBuildStage = buildStage;
			this.canRender = canRender;
		}
	}

}

