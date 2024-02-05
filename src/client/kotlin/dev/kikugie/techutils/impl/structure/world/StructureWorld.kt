package dev.kikugie.techutils.impl.structure.world

import dev.kikugie.worldrenderer.util.EntitySupplier
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.fluid.FluidState
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.Vec3i
import net.minecraft.world.BlockRenderView
import net.minecraft.world.LightType
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.biome.ColorResolver
import net.minecraft.world.chunk.light.LightingProvider
import kotlin.math.max
import kotlin.math.min

class StructureWorld : BlockRenderView, WorldWriter, EntitySupplier {
    private val regions = Long2ObjectOpenHashMap<StructureChunk>()
    private var x1: Int = 0
    private var y1: Int = 0
    private var z1: Int = 0
    private var x2: Int = 0
    private var y2: Int = 0
    private var z2: Int = 0
    val origin: BlockPos
        get() = BlockPos(x1, y1, z1)
    val end: BlockPos
        get() = BlockPos(x2, y2, z2)
    val size: Vec3i
        get() = Vec3i(x2 - x1 + 1, y2 - y1 + 1, z2 - z1 + 1)
    private val biome = MinecraftClient.getInstance().world!!.registryManager.get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS).value()

    override fun toString() = """
        origin=$origin
        end=$end
        regions=$regions
    """.trimIndent().replace("\n", ", ")

    fun trim() {
        regions.values.forEach { it.trim() }
    }

    override fun setBlockState(pos: BlockPos, state: BlockState) {
        if (state.isAir) return
        val region = getOrCreateRegion(pos)
        region[getLocalPos(pos)] = state
        expand(pos.x, pos.y, pos.z)
    }

    override fun setBlockEntity(pos: BlockPos, entity: BlockEntity) {
        val region = getOrCreateRegion(pos)
        region[getLocalPos(pos)] = entity
        expand(pos.x, pos.y, pos.z)
    }

    override fun setBlockEntityData(pos: BlockPos, nbt: NbtCompound) {
        val region = getOrCreateRegion(pos)
        region[getLocalPos(pos)] = nbt
        expand(pos.x, pos.y, pos.z)
    }

    override fun addEntity(entity: Entity) {
        val entityPos = entity.blockPos
        val region = getOrCreateRegion(entityPos)
        region.addEntity(entity)
        expand(entityPos.x, entityPos.y, entityPos.z)
    }

    override fun getHeight() = y2 - y1

    override fun getBottomY() = y1

    override fun getBlockEntity(pos: BlockPos): BlockEntity? = getRegion(pos)?.getBlockEntity(getLocalPos(pos))

    override fun getBlockState(pos: BlockPos): BlockState = getRegion(pos)?.getOrNull(getLocalPos(pos)) ?: AIR

    override fun getFluidState(pos: BlockPos): FluidState = getBlockState(pos).fluidState

    override fun getBrightness(direction: Direction, shaded: Boolean) = when (direction) {
        WEST, EAST -> 0.6F
        NORTH, SOUTH -> 0.8F
        DOWN -> 0.9F
        UP -> 1.0F
        else -> 1.0F
    }

    override fun getLightLevel(type: LightType, pos: BlockPos) = 15

    override fun getBaseLightLevel(pos: BlockPos, ambientDarkness: Int) = 15

    override fun getLightingProvider(): LightingProvider {
        throw UnsupportedOperationException("Lighting provider is not available, use light level accessors")
    }

    override fun getColor(pos: BlockPos, colorResolver: ColorResolver) = colorResolver.getColor(biome, pos.x.toDouble(), pos.z.toDouble())

    override fun invoke(): Iterable<Entity> = regions.values.asIterable().flatMap { it() }

    private fun getRegion(pos: BlockPos) = regions[getChunkPos(pos)]
    private fun getOrCreateRegion(pos: BlockPos) = getChunkPos(pos).let { local ->
        regions[getChunkPos(pos)] ?: StructureChunk().also {
            regions[local] = it
        }
    }

    private fun getLocalPos(pos: BlockPos) = BlockPos(pos.x and 15, pos.y, pos.z and 15)
    private fun getChunkPos(pos: BlockPos) = ChunkPos.toLong(pos.x shr 4, pos.z shr 4)

    private fun expand(x: Int, y: Int, z: Int) {
        x1 = min(x, x1)
        y1 = min(y, y1)
        z1 = min(z, z1)
        x2 = max(x, x2)
        y2 = max(y, y2)
        z2 = max(z, z2)
    }

    companion object {
        val AIR = Blocks.VOID_AIR.defaultState
    }
}