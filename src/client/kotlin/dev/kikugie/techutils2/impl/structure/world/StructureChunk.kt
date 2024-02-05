package dev.kikugie.techutils2.impl.structure.world

import dev.kikugie.techutils2.impl.structure.world.StructureWorld.Companion.AIR
import dev.kikugie.worldrenderer.util.EntitySupplier
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class StructureChunk : WorldWriter, EntitySupplier {
    private val blockStates = Long2ObjectOpenHashMap<BlockState>(4096)
    private val blockEntities = Long2ObjectOpenHashMap<BlockEntity>()
    private val entities = ArrayList<Entity>()
    override fun toString() = """
        blockStates=$blockStates
        blockEntities=$blockEntities
        entities=$entities
    """.trimIndent().replace("\n", ", ")

    operator fun get(pos: BlockPos): BlockState = blockStates[pos.asLong()] ?: AIR
    fun getOrNull(pos: BlockPos): BlockState? = blockStates[pos.asLong()]
    fun getBlockEntity(pos: BlockPos): BlockEntity? = blockEntities[pos.asLong()]
    override fun setBlockState(pos: BlockPos, state: BlockState) {
        if (!state.isAir) blockStates[pos.asLong()] = state
    }

    override fun setBlockEntity(pos: BlockPos, entity: BlockEntity) {
        blockEntities[pos.asLong()] = entity
    }

    override fun setBlockEntityData(pos: BlockPos, nbt: NbtCompound) {
        val state = getOrNull(pos)?: return
        val block = state.block as? BlockEntityProvider ?: return
        val blockEntity = block.createBlockEntity(pos, state) ?: return
        val copy = nbt.copy()
        with(copy) {
            putInt("x", pos.x)
            putInt("y", pos.y)
            putInt("z", pos.z)
        }
        blockEntity.readNbt(copy)
        this[pos] = blockEntity
    }

    override fun addEntity(entity: Entity) {
        entities += entity
    }
    override fun invoke(): Iterable<Entity> = entities.asIterable()

    fun trim() {
        blockStates.trim()
        blockEntities.trim()
        entities.trimToSize()
    }
}