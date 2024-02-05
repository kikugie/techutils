package dev.kikugie.techutils.impl.structure.load

import com.mojang.datafixers.util.Either
import dev.kikugie.techutils.impl.structure.Structure
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase
import java.nio.file.Path
import kotlin.io.path.*
import kotlin.jvm.Throws

typealias LoadResult = Either<Structure, Exception>

interface StructureLoader {
    val format: String

    @Throws(Exception::class)
    fun loadImpl(file: Path): Structure

    fun load(file: Path): LoadResult = try {
        Either.left(loadImpl(file))
    } catch (e: Exception) {
        Either.right(e)
    }

    companion object {
        fun load(file: Path): LoadResult = when (file.extension) {
            "litematic" -> LitematicLoader.load(file)
            "schematic" -> SchematicLoader.load(file)
            "schem" -> SpongeLoader.load(file)
            "nbt" -> VanillaLoader.load(file)
            "bp" -> BlueprintLoader.load(file)
            else -> LoadResult.right(IllegalArgumentException("Unsupported file format"))
        }

        fun load(entry: WidgetFileBrowserBase.DirectoryEntry) = load(entry.fullPath.toPath())
    }
}