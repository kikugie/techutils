package me.kikugie.techutils.render.litematica;

import fi.dy.masa.litematica.render.schematic.ChunkCacheSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.world.FakeLightingProvider;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class LitematicRegionBlockView implements BlockRenderView {
    public final Box box;
    public final Vec3i dimensions;
    private final LitematicaBlockStateContainer blockStateContainer;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final LightingProvider lightingProvider = new FakeLightingProvider(new ChunkCacheSchematic(client.world, client.world, new BlockPos(0, 0, 0), 0));

    public LitematicRegionBlockView(LitematicaBlockStateContainer container, Box area) {
        this.blockStateContainer = container;
        this.box = new Box(area.minX, area.minY, area.minZ, area.maxX + 1, area.maxY + 1, area.maxZ + 1);
        this.dimensions = container.getSize();
    }

    @Override
    public float getBrightness(Direction direction, boolean shaded) {
        return client.world.getBrightness(direction, shaded);
    }

    @Override
    public LightingProvider getLightingProvider() {
        return lightingProvider;
    }

    @Override
    public int getColor(BlockPos pos, ColorResolver colorResolver) {
        return client.world.getColor(pos, colorResolver);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        final var state = getBlockState(pos);
        if (state.getBlock() instanceof BlockEntityProvider provider) {
            return provider.createBlockEntity(pos, state);
        }
        return null;
    }

    /**
     * Returns BlockState in global schematic space
     */
    @Override
    public BlockState getBlockState(BlockPos pos) {
        if (!box.contains(Vec3d.of(pos))) return LitematicaBlockStateContainer.AIR_BLOCK_STATE;
        final var localPos = pos.subtract(getRegionOffset());
        return blockStateContainer.get(localPos.getX(), localPos.getY(), localPos.getZ());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return (int) box.getYLength();
    }

    @Override
    public int getBottomY() {
        return 0;
    }

    public Vec3i getRegionOffset() {
        return new Vec3i(box.minX, box.minY, box.minZ);
    }
}
