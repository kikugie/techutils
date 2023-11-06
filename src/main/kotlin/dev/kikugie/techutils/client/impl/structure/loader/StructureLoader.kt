package dev.kikugie.techutils.client.impl.structure.loader

import dev.kikugie.techutils.client.impl.structure.Structure
import java.io.IOException
import java.nio.file.Path

interface StructureLoader {
    val format: String

    @Throws(IOException::class)
    fun load(file: Path, lazy: Boolean): Structure

    fun loadException(file: Path, message: String): Exception = IOException("Failed to load  $file: $message")
}