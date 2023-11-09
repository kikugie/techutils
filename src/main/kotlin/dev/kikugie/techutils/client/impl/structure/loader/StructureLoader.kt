package dev.kikugie.techutils.client.impl.structure.loader

import dev.kikugie.techutils.client.impl.structure.Structure
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.malilib.interfaces.IStringConsumer
import java.io.IOException
import java.nio.file.Path

interface StructureLoader {
    val format: String

    @Throws(IOException::class)
    fun load(file: Path, lazy: Boolean): Structure

    @Throws(IOException::class)
    fun toLitematic(file: Path, feedback: IStringConsumer): LitematicaSchematic

    fun loadException(file: Path, message: String): Exception = IOException("Failed to load  $file: $message")
}