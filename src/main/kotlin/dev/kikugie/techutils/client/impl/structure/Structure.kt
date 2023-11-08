package dev.kikugie.techutils.client.impl.structure

import dev.kikugie.techutils.client.impl.structure.loader.BlueprintLoader
import dev.kikugie.techutils.client.impl.structure.loader.LitematicLoader
import dev.kikugie.techutils.client.impl.structure.loader.SchematicLoader
import dev.kikugie.techutils.client.impl.structure.loader.SpongeLoader
import dev.kikugie.techutils.client.impl.structure.world.StructureWorld
import dev.kikugie.techutils.client.util.computeIfLoaded
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase
import net.minecraft.client.texture.NativeImageBackedTexture
import java.nio.file.Path
import kotlin.io.path.extension

/**
 * @see <a href="https://xkcd.com/927/">Source</a>
 */
class Structure(
    val metadata: StructureMetadata,
    val preview: NativeImageBackedTexture? = null,
    val file: Path? = null,
    worldSupplier: () -> StructureWorld
) {
    constructor(
        metadata: StructureMetadata,
        preview: NativeImageBackedTexture? = null,
        file: Path? = null,
        world: StructureWorld
    ) : this(metadata, preview, file, { world }) {
        _world = world
    }

    private var _world: StructureWorld? = null
    val world: StructureWorld = if (_world != null) _world!! else {
        _world = worldSupplier()
        _world!!
    }

    companion object {
        private val loaders = mutableMapOf(
            "litematic" to LitematicLoader,
            "schem" to SpongeLoader,
            "schematic" to SchematicLoader,
            "nbt" to SchematicLoader
        )

        init {
            computeIfLoaded("axiom") { loaders["bp"] = BlueprintLoader }
        }

        @Throws(Exception::class)
        fun load(entry: WidgetFileBrowserBase.DirectoryEntry, lazy: Boolean = false): Structure {
            val file = entry.fullPath.toPath()
            return loaders[file.extension]?.load(file, lazy)
                ?: throw IllegalArgumentException("Unsupported file format: ${file.extension}")
        }
    }
}