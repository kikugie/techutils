package dev.kikugie.techutils.client.feature.preview.world

import dev.kikugie.techutils.Reference
import fi.dy.masa.litematica.util.PositionUtils
import fi.dy.masa.litematica.world.ChunkSchematic
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.item.map.MapState
import net.minecraft.recipe.RecipeManager
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.*
import net.minecraft.util.profiler.Profiler
import net.minecraft.world.LightType
import net.minecraft.world.MutableWorldProperties
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.chunk.ChunkManager
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.entity.EntityLookup
import net.minecraft.world.event.GameEvent
import net.minecraft.world.tick.QueryableTickScheduler
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier

class LitematicRenderWorld(
    properties: MutableWorldProperties,
    dimension: RegistryEntry<DimensionType>,
    supplier: Supplier<Profiler>,
) : World(
    properties,
    REGISTRY_KEY,
    MinecraftClient.getInstance().networkHandler!!.registryManager,
    dimension,
    supplier,
    true,
    false,
    0L,
    0
) {
    private val client = MinecraftClient.getInstance()
    private val biome: RegistryEntry<Biome>
    private val chunkManager: LitematicChunkManager = LitematicChunkManager(this)
    private var nextEntityId = 0

    init {
        biome = client.world!!.registryManager.get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS)
    }

    fun loadChunks(origin: BlockPos, areaSize: Vec3i) {
        val posEnd = origin.add(PositionUtils.getRelativeEndPositionFromAreaSize(areaSize))
        val posMin = PositionUtils.getMinCorner(origin, posEnd)
        val posMax = PositionUtils.getMaxCorner(origin, posEnd)
        val cxMin = posMin.x shr 4
        val czMin = posMin.z shr 4
        val cxMax = posMax.x shr 4
        val czMax = posMax.z shr 4
        for (cz in czMin..czMax) for (cx in cxMin..cxMax) chunkManager.loadChunk(cx, cz)
    }

    override fun setBlockState(pos: BlockPos, state: BlockState, flags: Int): Boolean {
        return this.getChunk(pos.x shr 4, pos.z shr 4).setBlockState(pos, state, false) != null
    }

    override fun spawnEntity(entity: Entity): Boolean {
        val cx = MathHelper.floor(entity.x / 16)
        val cz = MathHelper.floor(entity.z / 16)
        entity.id = nextEntityId++
        chunkManager.getChunk(cx, cz).addEntity(entity)
        return true
    }

    val allEntities: List<Entity>
        get() {
            val entities: MutableList<Entity> = ArrayList()
            for (chunk in chunkManager.loadedChunks.values) {
                entities.addAll(chunk.entityList)
            }
            return entities
        }

    override fun getOtherEntities(except: Entity?, box: Box, predicate: Predicate<in Entity>): List<Entity> {
        val entities: MutableList<Entity> = ArrayList()
        for (chunk in getChunksWithinBox(box)) {
            chunk.entityList.forEach(Consumer { e: Entity ->
                if (e !== except && box.intersects(e.boundingBox) && predicate.test(e)) {
                    entities.add(e)
                }
            })
        }
        return entities
    }

    override fun getLightLevel(type: LightType, pos: BlockPos): Int {
        return 15
    }

    override fun getBaseLightLevel(pos: BlockPos, defaultValue: Int): Int {
        return 15
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

    override fun updateListeners(pos: BlockPos, oldState: BlockState, newState: BlockState, flags: Int) {}
    override fun playSound(
        except: PlayerEntity?,
        x: Double,
        y: Double,
        z: Double,
        sound: RegistryEntry<SoundEvent>,
        category: SoundCategory,
        volume: Float,
        pitch: Float,
        seed: Long
    ) {
    }

    override fun playSoundFromEntity(
        except: PlayerEntity?,
        entity: Entity,
        sound: RegistryEntry<SoundEvent>,
        category: SoundCategory,
        volume: Float,
        pitch: Float,
        seed: Long
    ) {
    }

    override fun asString(): String {
        return "null"
    }

    override fun getEntityById(id: Int): Entity? {
        return null
    }

    override fun getMapState(id: String): MapState? {
        return null
    }

    override fun putMapState(id: String, state: MapState) {}
    override fun getNextMapId(): Int {
        return 0
    }

    override fun setBlockBreakingInfo(entityId: Int, pos: BlockPos, progress: Int) {}
    override fun getScoreboard(): Scoreboard? {
        return null
    }

    override fun getRecipeManager(): RecipeManager? {
        return null
    }

    override fun getEntityLookup(): EntityLookup<Entity>? {
        return null
    }

    override fun getBlockTickScheduler(): QueryableTickScheduler<Block>? {
        return null
    }

    override fun getFluidTickScheduler(): QueryableTickScheduler<Fluid>? {
        return null
    }

    override fun getChunkManager(): ChunkManager {
        return chunkManager
    }

    override fun syncWorldEvent(player: PlayerEntity?, eventId: Int, pos: BlockPos, data: Int) {}
    override fun emitGameEvent(event: GameEvent, emitterPos: Vec3d, emitter: GameEvent.Emitter) {}
    override fun getBrightness(direction: Direction, shaded: Boolean): Float {
        return client.world!!.getBrightness(direction, shaded)
    }

    override fun getPlayers(): List<PlayerEntity> {
        return emptyList()
    }

    override fun getGeneratorStoredBiome(biomeX: Int, biomeY: Int, biomeZ: Int): RegistryEntry<Biome> {
        return biome
    }

    override fun getEnabledFeatures(): FeatureSet {
        return FeatureFlags.DEFAULT_ENABLED_FEATURES
    }

    companion object {
        val REGISTRY_KEY: RegistryKey<World> = RegistryKey.of(RegistryKeys.WORLD, Reference.id("render_world"))
    }
}
