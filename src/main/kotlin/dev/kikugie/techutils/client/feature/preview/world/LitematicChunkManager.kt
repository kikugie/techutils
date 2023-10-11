package dev.kikugie.techutils.client.feature.preview.world

import fi.dy.masa.litematica.world.ChunkSchematic
import fi.dy.masa.litematica.world.FakeLightingProvider
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.BlockView
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkManager
import net.minecraft.world.chunk.ChunkStatus
import net.minecraft.world.chunk.light.LightingProvider
import java.util.function.BooleanSupplier

class LitematicChunkManager(private val world: LitematicRenderWorld) : ChunkManager() {
    private val emptyChunk: ChunkSchematic = ChunkSchematic(world, ChunkPos(0, 0))
    private val lightingProvider: LightingProvider
    @JvmField
    var loadedChunks: Long2ObjectMap<ChunkSchematic> = Long2ObjectOpenHashMap(16)

    init {
        lightingProvider = FakeLightingProvider(this)
    }

    fun loadChunk(chunkX: Int, chunkZ: Int) {
        val chunk = ChunkSchematic(world, ChunkPos(chunkX, chunkZ))
        loadedChunks.put(ChunkPos.toLong(chunkX, chunkZ), chunk)
    }

    override fun getChunk(x: Int, z: Int): ChunkSchematic {
        return loadedChunks.getOrDefault(ChunkPos.toLong(x, z), emptyChunk)
    }

    override fun getChunk(x: Int, z: Int, leastStatus: ChunkStatus, create: Boolean): Chunk? {
        return loadedChunks.getOrDefault(ChunkPos.toLong(x, z), if (create) emptyChunk else null)
    }

    fun getChunkIfExists(chunkX: Int, chunkZ: Int): ChunkSchematic {
        return loadedChunks[ChunkPos.toLong(chunkX, chunkZ)] as ChunkSchematic
    }

    override fun tick(shouldKeepTicking: BooleanSupplier, tickChunks: Boolean) {}
    override fun getDebugString(): String {
        return "null"
    }

    override fun getLoadedChunkCount(): Int {
        return loadedChunks.size
    }

    override fun getLightingProvider(): LightingProvider {
        return lightingProvider
    }

    override fun getWorld(): BlockView {
        return world
    }
}
