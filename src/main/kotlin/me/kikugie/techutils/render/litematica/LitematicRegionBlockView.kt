package me.kikugie.techutils.render.litematica

import fi.dy.masa.litematica.render.schematic.ChunkCacheSchematic
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer
import fi.dy.masa.litematica.world.FakeLightingProvider
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.fluid.FluidState
import net.minecraft.util.math.*
import net.minecraft.world.BlockRenderView
import net.minecraft.world.biome.ColorResolver
import net.minecraft.world.chunk.light.LightingProvider

class LitematicRegionBlockView(private val blockStateContainer: LitematicaBlockStateContainer?, area: Box?) :
    BlockRenderView {
    private val box: Box
    private val dimensions: Vec3i
    private val client = MinecraftClient.getInstance()
    private val lightingProvider: LightingProvider =
        FakeLightingProvider(ChunkCacheSchematic(client.world, client.world, BlockPos(0, 0, 0), 0))

    init {
        box = Box(area!!.minX, area.minY, area.minZ, area.maxX + 1, area.maxY + 1, area.maxZ + 1)
        dimensions = blockStateContainer!!.size
    }

    override fun getBrightness(direction: Direction, shaded: Boolean): Float {
        return client.world!!.getBrightness(direction, shaded)
    }

    override fun getLightingProvider(): LightingProvider {
        return lightingProvider
    }

    override fun getColor(pos: BlockPos, colorResolver: ColorResolver): Int {
        return client.world!!.getColor(pos, colorResolver)
    }

    override fun getBlockEntity(pos: BlockPos): BlockEntity? {
        val state = getBlockState(pos)
        return if (state.block is BlockEntityProvider) {
            null
//            provider.createBlockEntity(pos, state)
        } else null
    }

    /**
     * Returns BlockState in global schematic space
     */
    override fun getBlockState(pos: BlockPos): BlockState {
        if (!box.contains(Vec3d.of(pos))) return LitematicaBlockStateContainer.AIR_BLOCK_STATE
        val localPos = pos.subtract(regionOffset)
        return blockStateContainer!![localPos.x, localPos.y, localPos.z]
    }

    override fun getFluidState(pos: BlockPos): FluidState {
        return getBlockState(pos).fluidState
    }

    override fun getHeight(): Int {
        return box.yLength.toInt()
    }

    override fun getBottomY(): Int {
        return 0
    }

    private val regionOffset: Vec3i
        get() = Vec3i(box.minX, box.minY, box.minZ)
}