package dev.kikugie.techutils.client.impl.structure.world

import dev.kikugie.techutils.Reference
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.item.map.MapState
import net.minecraft.recipe.RecipeManager
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.*
import net.minecraft.world.Difficulty
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeKeys
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.entity.EntityLookup
import net.minecraft.world.event.GameEvent
import net.minecraft.world.tick.EmptyTickSchedulers
import net.minecraft.world.tick.QueryableTickScheduler
import java.util.*


abstract class DummyWorld(protected val client: MinecraftClient) : World(
    PROPERTIES,
    REGISTRY_KEY,
    RegistryEntry.of(DIMENSION_TYPE),
    { client.profiler },
    true,
    false,
    0L
) {
    private val biome = RegistryEntry.of(BuiltinRegistries.BIOME[BiomeKeys.PLAINS]!!)

    companion object {
        val REGISTRY_KEY: RegistryKey<World> = RegistryKey.of(Registry.WORLD_KEY, Reference.id("render_world"))
        val PROPERTIES = ClientWorld.Properties(Difficulty.PEACEFUL, false, true)
        val DIMENSION_TYPE = DimensionType.create(
            OptionalLong.of(6000L),
            true,
            false,
            false,
            false,
            1.0,
            false,
            false,
            false,
            false,
            false,
            -64,
            384,
            384,
            BlockTags.INFINIBURN_END,
            DimensionType.OVERWORLD_ID,
            1.0f
        )
    }

    override fun getPlayers() = emptyList<PlayerEntity>()

    override fun getBrightness(direction: Direction?, shaded: Boolean) = when (direction) {
        Direction.DOWN -> 0.9f
        Direction.UP -> 1.0f
        Direction.NORTH, Direction.SOUTH -> 0.8f
        Direction.WEST, Direction.EAST -> 0.6f
        else -> 1.0f
    }

    override fun getGeneratorStoredBiome(biomeX: Int, biomeY: Int, biomeZ: Int): RegistryEntry<Biome> = biome
    override fun getRegistryManager(): DynamicRegistryManager = client.world!!.registryManager

    override fun getBlockTickScheduler(): QueryableTickScheduler<Block> = EmptyTickSchedulers.getClientTickScheduler()

    override fun getFluidTickScheduler(): QueryableTickScheduler<Fluid> = EmptyTickSchedulers.getClientTickScheduler()

    override fun playSound(
        except: PlayerEntity?,
        x: Double,
        y: Double,
        z: Double,
        sound: SoundEvent?,
        category: SoundCategory?,
        volume: Float,
        pitch: Float
    ) {
    }

    override fun syncWorldEvent(player: PlayerEntity?, eventId: Int, pos: BlockPos?, data: Int) {
    }

    override fun emitGameEvent(entity: Entity?, event: GameEvent?, pos: BlockPos?) {
    }

    override fun updateListeners(pos: BlockPos?, oldState: BlockState?, newState: BlockState?, flags: Int) {
    }

    override fun playSoundFromEntity(
        except: PlayerEntity?,
        entity: Entity?,
        sound: SoundEvent?,
        category: SoundCategory?,
        volume: Float,
        pitch: Float
    ) {
    }

    override fun asString() = "Chunks[TU] W:${chunkManager.loadedChunkCount} chunks"

    override fun getEntityById(id: Int): Entity? = null

    override fun getMapState(id: String?): MapState? = null

    override fun putMapState(id: String?, state: MapState?) {}

    override fun getNextMapId() = 0

    override fun setBlockBreakingInfo(entityId: Int, pos: BlockPos?, progress: Int) {
    }

    override fun getScoreboard(): Scoreboard? = client.world?.scoreboard

    override fun getRecipeManager(): RecipeManager? = client.world?.recipeManager

    override fun getEntityLookup(): EntityLookup<Entity>? = null
}