package dev.kikugie.techutils.client.impl.structure.world

import dev.kikugie.techutils.client.util.data.Pos2ObjectMap
import fi.dy.masa.litematica.world.ChunkSchematic
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.BlockView
import net.minecraft.world.chunk.ChunkManager
import net.minecraft.world.chunk.ChunkStatus
import net.minecraft.world.chunk.light.LightingProvider
import java.util.function.BooleanSupplier

class StructureChunkManager(private val world: StructureWorld) :  ChunkManager() {
    private val lightingProvider: LightingProvider = LightingProvider(this, true, world.dimension.hasSkyLight())
    internal val chunks = Pos2ObjectMap { ChunkSchematic(world, ChunkPos(it)) }

    fun get(x: Int, z: Int, create: Boolean = true) = getChunk(x, z, null, create)
    override fun getChunk(x: Int, z: Int, leastStatus: ChunkStatus?, create: Boolean): ChunkSchematic {
        return if (create) chunks.compute(ChunkPos.toLong(x, z)) else chunks[ChunkPos.toLong(x, z)]
    }

    fun getChunkIfExists(chunkX: Int, chunkZ: Int): ChunkSchematic? {
        val key = ChunkPos.toLong(chunkX, chunkZ)
        return if (chunks.containsKey(key)) chunks[key] else null
    }

    override fun getWorld(): BlockView = world

    override fun tick(shouldKeepTicking: BooleanSupplier?, tickChunks: Boolean) {
    }

    override fun getDebugString() = "StructureChunkManager: ${chunks.size} chunks"

    override fun getLoadedChunkCount() = chunks.size

    override fun getLightingProvider() = lightingProvider
}