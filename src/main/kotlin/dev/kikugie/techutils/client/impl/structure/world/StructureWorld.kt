package dev.kikugie.techutils.client.impl.structure.world

import dev.kikugie.techutils.client.TechUtilsClient
import dev.kikugie.techutils.client.util.data.IntBox
import dev.kikugie.techutils.client.util.multiversion.entities
import fi.dy.masa.litematica.world.ChunkSchematic
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import java.util.function.Consumer
import java.util.function.Predicate

class StructureWorld(val name: String? = null) : DummyWorld(MinecraftClient.getInstance()) {
    private val manager = StructureChunkManager(this)

    private var blocks = 0
    private var entities = 0
    private var nextEntityId = 1
    private val box = IntBox()
    val size = box.size!!
    val volume = box.volume
    override fun getChunkManager(): StructureChunkManager = manager

    override fun setBlockState(pos: BlockPos, state: BlockState, flags: Int): Boolean {
        if (!state.isAir) blocks++
        val chunk = getChunk(pos.x shr 4, pos.z shr 4, null, true)
        if (chunk == null) {
            TechUtilsClient.LOGGER.warn("Failed to load chunk for block at $pos in structure world $name")
            return false
        }
        return (chunk.setBlockState(pos, state, false) != null).also { if (it) box.extend(pos) }
    }

    fun setBlockEntity(pos: BlockPos, tag: NbtCompound) {
        val chunk = getChunk(pos.x shr 4, pos.z shr 4, null, true)
        if (chunk == null) {
            TechUtilsClient.LOGGER.warn("Failed to load chunk for block entity at $pos in structure world $name")
            return
        }
        chunk.setBlockEntity(BlockEntity.createFromNbt(pos, chunk.getBlockState(pos), tag))
    }

    override fun spawnEntity(entity: Entity): Boolean {
        val cx = MathHelper.floor(entity.x / 16)
        val cz = MathHelper.floor(entity.z / 16)
        entity.id = nextEntityId++
        entities++
        chunkManager.get(cx, cz, true).addEntity(entity)
        return true
    }

    val allEntities: List<Entity>
        get() {
            val entities: MutableList<Entity> = ArrayList()
            for (chunk in chunkManager.chunks.values) {
                entities.addAll(chunk.entities())
            }
            return entities
        }

    override fun getOtherEntities(except: Entity?, box: Box, predicate: Predicate<in Entity>): List<Entity> {
        val entities: MutableList<Entity> = ArrayList()
        for (chunk in getChunksWithinBox(box)) {
            chunk.entities().forEach(Consumer { e: Entity ->
                if (e !== except && box.intersects(e.boundingBox) && predicate.test(e)) {
                    entities.add(e)
                }
            })
        }
        return entities
    }

    private fun getChunksWithinBox(box: Box): List<ChunkSchematic> {
        val minX = MathHelper.floor(box.minX / 16.0)
        val minZ = MathHelper.floor(box.minZ / 16.0)
        val maxX = MathHelper.floor(box.maxX / 16.0)
        val maxZ = MathHelper.floor(box.maxZ / 16.0)
        val chunks: MutableList<ChunkSchematic> = ArrayList()
        for (cx in minX..maxX) {
            for (cz in minZ..maxZ) {
                val chunk = chunkManager.getChunkIfExists(cx, cz)
                if (chunk != null) {
                    chunks.add(chunk)
                }
            }
        }
        return chunks
    }
}