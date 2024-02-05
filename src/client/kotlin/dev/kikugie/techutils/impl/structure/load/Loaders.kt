package dev.kikugie.techutils.impl.structure.load

import com.mojang.serialization.Dynamic
import dev.kikugie.techutils.impl.structure.Structure
import dev.kikugie.techutils.impl.structure.StructureMetadata
import dev.kikugie.techutils.impl.structure.world.StructureWorld
import dev.kikugie.techutils.impl.structure.world.WorldWriter
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.util.FileType
import net.minecraft.SharedConstants
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.datafixer.Schemas
import net.minecraft.datafixer.TypeReferences
import net.minecraft.nbt.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.chunk.PalettedContainer
import java.io.DataInputStream
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.readAttributes

private fun fromLitematic(file: Path, litematic: LitematicaSchematic) =
    StructureMetadata.create(
        litematic.metadata.name,
        litematic.metadata.author,
        litematic.totalSize,
        litematic.metadata.totalBlocks,
        litematic.metadata.totalVolume,
        litematic.metadata.timeCreated,
        litematic.metadata.timeModified
    ).let { Structure(file, it) { createWorld(litematic) } }

private fun createWorld(litematic: LitematicaSchematic) = StructureWorld().also { litematic.placeTo(it) }

private fun loadDelegate(file: Path, type: FileType, message: String) =
    LitematicaSchematic.createFromFile(
        file.parent.toFile(),
        file.fileName.toString(),
        type
    )?.let { fromLitematic(file, it) } ?: throw IOException(message)

object LitematicLoader : StructureLoader {
    override val format = "litematic"
    override fun loadImpl(file: Path) = loadDelegate(file, FileType.LITEMATICA_SCHEMATIC, "Invalid litematic data")
}

object SpongeLoader : StructureLoader {
    override val format = "schem"

    override fun loadImpl(file: Path) = loadDelegate(file, FileType.SPONGE_SCHEMATIC, "Invalid schem data")
}

object SchematicLoader : StructureLoader {
    override val format = "schematic"

    override fun loadImpl(file: Path) = loadDelegate(file, FileType.SCHEMATICA_SCHEMATIC, "Invalid schematic data")
}

object VanillaLoader : StructureLoader {
    override val format = "nbt"

    override fun loadImpl(file: Path) = loadDelegate(file, FileType.VANILLA_STRUCTURE, "Invalid structure data")
}

object BlueprintLoader : StructureLoader {
    override val format = "bp"
    private const val MAGIC = 182827830 // Used by Axiom to verify if the file has been corrupted.
    private val dataVersion = SharedConstants.getGameVersion().saveVersion.id
    private val BLOCK_STATE_CODEC = PalettedContainer.createPalettedContainerCodec(
        Block.STATE_IDS,
        BlockState.CODEC,
        PalettedContainer.PaletteProvider.BLOCK_STATE,
        Blocks.STRUCTURE_VOID.defaultState
    )

    override fun loadImpl(file: Path): Structure {
        val input = verifyFile(file)
        val metadata = readMetadata(file, NbtIo.read(input))
        return Structure(file, metadata, readWorld(input, metadata))
    }

    private fun verifyFile(file: Path): DataInputStream {
        val inputStream = file.inputStream()
        if (inputStream.available() < 4)
            throw IOException("Empty file")
        return DataInputStream(inputStream).apply {
            if (readInt() != MAGIC) throw IOException("Corrupted file")
            readInt()
        }
    }

    // To fill in: size, volume
    private fun readMetadata(file: Path, nbt: NbtCompound): StructureMetadata {
        val attrs = file.readAttributes<BasicFileAttributes>()
        return StructureMetadata.create {
            name = nbt.getString("name").ifBlank { file.name }
            author = nbt.getString("author").ifBlank { "???" }
            blocks = nbt.getInt("blockCount")
            timeCreated = attrs.creationTime().toMillis()
            timeModified = attrs.lastModifiedTime().toMillis()

            size = Vec3i(-1, -1, -1)
            volume = -1
        }
    }

    private fun readWorld(input: DataInputStream, metadata: StructureMetadata?): () -> StructureWorld {
        input.readInt()
        val blockDataTag = NbtIo.readCompressed(input)
        return {
            createWorldSupplier(blockDataTag).also {
                metadata?.size = it.size
                metadata?.volume = it.size.let { s -> s.x * s.y * s.z }
            }
        }
    }

    private fun createWorldSupplier(nbt: NbtCompound) = StructureWorld().also {
        var blueprintDataVersion = nbt.getInt("DataVersion")
        if (blueprintDataVersion == 0) blueprintDataVersion = dataVersion

        val regionTag = nbt.getList("BlockRegion", NbtElement.COMPOUND_TYPE.toInt())
        readChunkData(it, regionTag)

        val blockEntitiesTag = nbt.getList("BlockEntities", NbtElement.COMPOUND_TYPE.toInt())
        readBlockEntities(it, blockEntitiesTag, blueprintDataVersion)
    }

    private fun readBlockEntities(world: WorldWriter, data: NbtList, blueprintVersion: Int) {
        val upgrage = dataVersion != blueprintVersion
        for (tag in data) {
            val beTag = if (upgrage) {
                val dynamic = Dynamic(NbtOps.INSTANCE, tag)
                val output =
                    Schemas.getFixer().update(TypeReferences.BLOCK_ENTITY, dynamic, blueprintVersion, dataVersion)
                output.value as NbtCompound
            } else tag as NbtCompound

            val pos = BlockEntity.posFromNbt(beTag)
            world[pos] = beTag
        }
    }

    private fun readChunkData(world: WorldWriter, data: NbtList) {
        for (tag in data) {
            if (tag !is NbtCompound) continue

            val cx = tag.getInt("X")
            val cy = tag.getInt("Y")
            val cz = tag.getInt("Z")
            val blockStates = updatePalettedContainer(tag.getCompound("BlockStates"))
            val container: PalettedContainer<BlockState> =
                BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, blockStates).result().orElseThrow { IOException("// TODO") }

            for (pos in BlockPos.iterate(BlockPos.ORIGIN, BlockPos(15, 15, 15)))
                container[pos.x, pos.y, pos.z]?.let {
                    world[BlockPos(
                        cx * 16 + pos.x,
                        cy * 16 + pos.y,
                        cz * 16 + pos.z
                    )] = it
                }

        }
    }

    private fun updatePalettedContainer(tag: NbtCompound): NbtCompound {
        return if (!hasExpectedPaletteTag(tag)) {
            tag
        } else {
            val copy = tag.copy()
            val newPalette = NbtList()
            for (entry in copy.getList("palette", 10)) {
                val dynamic = Dynamic(NbtOps.INSTANCE, entry)
                val output =
                    Schemas.getFixer().update(TypeReferences.BLOCK_STATE, dynamic, dataVersion, dataVersion)
                newPalette.add(output.value as NbtElement)
            }
            copy.put("palette", newPalette)
            copy
        }
    }

    private fun hasExpectedPaletteTag(tag: NbtCompound): Boolean {
        return if (!tag.contains("palette", 9)) {
            false
        } else {
            val listTag = tag["palette"] as NbtList?
            if (listTag == null) {
                false
            } else {
                listTag.isEmpty() || listTag.heldType.toInt() == 10
            }
        }
    }
}