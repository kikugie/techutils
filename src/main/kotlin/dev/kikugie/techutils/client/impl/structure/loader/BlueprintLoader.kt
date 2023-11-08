package dev.kikugie.techutils.client.impl.structure.loader

import com.mojang.serialization.Dynamic
import dev.kikugie.techutils.client.TechUtilsClient
import dev.kikugie.techutils.client.impl.structure.Structure
import dev.kikugie.techutils.client.impl.structure.StructureMetadata
import dev.kikugie.techutils.client.impl.structure.world.StructureWorld
import dev.kikugie.techutils.client.util.data.iterate
import dev.kikugie.techutils.client.util.getString
import dev.kikugie.techutils.client.util.updatePalettedContainer
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.selection.AreaSelection
import fi.dy.masa.litematica.selection.Box
import fi.dy.masa.malilib.interfaces.IStringConsumer
import net.minecraft.SharedConstants
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.datafixer.Schemas
import net.minecraft.datafixer.TypeReferences
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.chunk.PalettedContainer
import org.lwjgl.system.MemoryStack
import java.io.DataInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.inputStream

object BlueprintLoader : StructureLoader {
    override val format: String = "bp"
    private const val MAGIC = 182827830
    private val BLOCK_STATE_CODEC = PalettedContainer.createPalettedContainerCodec(
        Block.STATE_IDS,
        BlockState.CODEC,
        PalettedContainer.PaletteProvider.BLOCK_STATE,
        Blocks.STRUCTURE_VOID.defaultState
    )

    @Throws(Exception::class)
    fun toLitematic(file: Path, feedback: IStringConsumer): LitematicaSchematic {
        val structure = load(file, false)
        val selection = AreaSelection()
        selection.addSubRegionBox(
            Box(BlockPos.ORIGIN, BlockPos(structure.metadata.size), structure.metadata.name),
            false
        )
        val info = LitematicaSchematic.SchematicSaveInfo(false, false)
        return LitematicaSchematic.createFromWorld(
            structure.world,
            selection,
            info,
            structure.metadata.author,
            feedback
        ) ?: throw IllegalStateException("Failed to create Litematica schematic")
    }

    override fun load(file: Path, lazy: Boolean): Structure {
        val input = verifyFile(file)
        val metadata = readMetadata(NbtIo.readCompound(input), file)
        val thumbnail = try {
            readThumbnail(file, input)
        } catch (e: IOException) {
            TechUtilsClient.LOGGER.warn(e.message)
            null
        }

        return if (lazy) {
            metadata.size = Vec3i.ZERO
            metadata.volume = 0
            Structure(metadata.toImmutable(), thumbnail, file) { createWorld(file, input) }
        } else {
            val world = createWorld(file, input)
            metadata.size = world.size
            metadata.volume = world.volume
            Structure(metadata.toImmutable(), thumbnail, file, world)
        }
    }

    private fun createWorld(file: Path, input: DataInputStream): StructureWorld {
        val dataVersion = SharedConstants.getGameVersion().saveVersion.id
        input.readInt()
        val blockDataTag = NbtIo.readCompressed(input)
        var blueprintDataVersion = blockDataTag.getInt("DataVersion")
        if (blueprintDataVersion == 0) blueprintDataVersion = dataVersion

        val regionTag = blockDataTag.getList("BlockRegion", 10)
        val world = readChunkData(file, regionTag, dataVersion)

        val blockEntitiesTag = blockDataTag.getList("BlockEntities", 10)
        readBlockEntities(blockEntitiesTag, world, dataVersion, blueprintDataVersion)
        return world
    }

    private fun readBlockEntities(
        list: NbtList,
        world: StructureWorld,
        dataVersion: Int,
        blueprintDataVersion: Int
    ) {
        val upgrage = dataVersion != blueprintDataVersion
        for (tag in list) {
            val beTag = if (upgrage) {
                val dynamic = Dynamic(NbtOps.INSTANCE, tag)
                val output =
                    Schemas.getFixer().update(TypeReferences.BLOCK_ENTITY, dynamic, blueprintDataVersion, dataVersion)
                output.value as NbtCompound
            } else tag as NbtCompound

            val pos = BlockEntity.posFromNbt(beTag)
            world.setBlockEntity(pos, beTag)
        }
    }

    private fun readMetadata(nbt: NbtCompound, file: Path): StructureMetadata.Mutable {
        val attrs = Files.readAttributes(file, BasicFileAttributes::class.java)
        return StructureMetadata.Mutable(
            name = nbt.getString("name", file.fileName.toString()),
            author = nbt.getString("author", "Unknown"),
            size = null,
            blocks = nbt.getInt("blockCount"),
            volume = null,
            timeCreated = attrs.creationTime().toMillis(),
            timeModified = attrs.lastModifiedTime().toMillis(),
        )
    }

    private fun readThumbnail(file: Path, dataInputStream: DataInputStream): NativeImageBackedTexture {
        val thumbnailLength = dataInputStream.readInt()
        val thumbnailBytes = dataInputStream.readNBytes(thumbnailLength)
        if (thumbnailBytes.size < thumbnailLength)
            throw loadException(file, "Missing thumbnail data")
        val memoryStack = MemoryStack.stackPush()
        val nativeImage: NativeImage = try {
            val byteBuffer = memoryStack.malloc(thumbnailBytes.size)
            byteBuffer.put(thumbnailBytes)
            byteBuffer.rewind()
            NativeImage.read(byteBuffer)
        } catch (var30: Throwable) {
            try {
                memoryStack.close()
            } catch (var29: Throwable) {
                var30.addSuppressed(var29)
            }
            throw var30
        }
        memoryStack.close()
        return NativeImageBackedTexture(nativeImage)
    }

    private fun verifyFile(file: Path): DataInputStream {
        val inputStream = file.inputStream()
        if (inputStream.available() < 4)
            throw loadException(file, "Empty file")
        val dataInputStream = DataInputStream(inputStream)
        if (dataInputStream.readInt() != MAGIC)
            throw loadException(file, "Corrupted file")
        dataInputStream.readInt()
        return dataInputStream
    }

    private fun readChunkData(file: Path, list: NbtList, dataVersion: Int): StructureWorld {
        val world = StructureWorld()
        for (tag in list) {
            if (tag !is NbtCompound) continue

            val cx = tag.getInt("X")
            val cy = tag.getInt("Y")
            val cz = tag.getInt("Z")
            val blockStates = updatePalettedContainer(tag.getCompound("BlockStates"), dataVersion)
            val container: PalettedContainer<BlockState> =
                BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, blockStates).result().orElseThrow {
                    loadException(file, "Failed to parse block states")
                }

            iterate(15, 15, 15) { pos ->
                container[pos.x, pos.y, pos.z]
                    .takeIf { it.block != Blocks.STRUCTURE_VOID }
                    ?.let { world.setBlockState(BlockPos(cx * 16 + pos.x, cy * 16 + pos.y, cz * 16 + pos.z), it, 0) }
            }
        }
        return world
    }
}