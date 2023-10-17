package dev.kikugie.techutils.client.feature.preview.util

import dev.kikugie.techutils.Reference
import dev.kikugie.techutils.client.TechUtilsClient
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import kotlin.math.sqrt

object LitematicaUtils {
    fun loadLitematicaPreview(litematic: LitematicaSchematic): NativeImageBackedTexture? {
        val pixelData = litematic.metadata.previewImagePixelData ?: return null
        if (pixelData.isEmpty()) return null

        val path =
            if (litematic.file != null) litematic.file!!.absolutePath else "${litematic.metadata.name} (in-memory)"
        val size = sqrt(pixelData.size.toDouble()).toInt()
        if (size * size != pixelData.size) return null
        try {
            val image = NativeImage(size, size, false)
            val texture = NativeImageBackedTexture(image)
            val id = Reference.id(DigestUtils.sha1Hex(path))
            MinecraftClient.getInstance().textureManager.registerTexture(id, texture)
            for (y in 0 until size) for (x in 0 until size) {
                val pixel = pixelData[y * size + x]
                image.setColor(
                    x, y, pixel and -0xff0100 or (pixel and 0xFF0000 shr 16) or (pixel and 0xFF shl 16)
                )
            }
            texture.upload()
            return texture
        } catch (e: Exception) {
            TechUtilsClient.LOGGER.warn("Failed to load preview image for schematic $path", e)
            return null
        }
    }
}