package dev.kikugie.techutils.feature.preview.model;

import dev.kikugie.techutils.util.ValidBox;
import fi.dy.masa.litematica.render.schematic.ChunkCacheSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.PositionUtils;
import fi.dy.masa.litematica.world.FakeLightingProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class RegionBlockView implements BlockRenderView {
	public final ValidBox box;
	private final LitematicaBlockStateContainer blockStateContainer;
	private final MinecraftClient client = MinecraftClient.getInstance();
	private final LightingProvider lightingProvider = new FakeLightingProvider(new ChunkCacheSchematic(this.client.world, this.client.world, new BlockPos(0, 0, 0), 0));

	public RegionBlockView(LitematicaBlockStateContainer container, Box area) {
		this.blockStateContainer = container;
		this.box = ValidBox.of(area);
	}

	@Override
	public float getBrightness(Direction direction, boolean shaded) {
		assert this.client.world != null;
		return this.client.world.getBrightness(direction, shaded);
	}

	@Override
	public LightingProvider getLightingProvider() {
		return this.lightingProvider;
	}

	@Override
	public int getColor(BlockPos pos, ColorResolver colorResolver) {
		return this.client.world.getColor(pos, colorResolver);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (!PositionUtils.isPositionInsideArea(pos, this.box.getMin(), this.box.getMax()))
			return LitematicaBlockStateContainer.AIR_BLOCK_STATE;
		BlockPos local = pos.subtract(this.box.getMin());
		return this.blockStateContainer.get(local.getX(), local.getY(), local.getZ());
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}

	@Override
	public int getHeight() {
		return this.box.getSize().getY();
	}

	@Override
	public int getBottomY() {
		return 0;
	}
}
