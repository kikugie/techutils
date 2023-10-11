package dev.kikugie.techutils.client.feature.preview

import dev.kikugie.techutils.config.ConfigGroup
import fi.dy.masa.malilib.config.options.ConfigDouble
import fi.dy.masa.malilib.config.options.ConfigInteger
import net.fabricmc.loader.api.FabricLoader

object PreviewConfig : ConfigGroup {
    val previewAngle = ConfigInteger("previewAngle", 45, 0, 360, "Angle of the preview")
    val previewSlant = ConfigInteger("previewSlant", 30, 0, 90, "Slant of the preview")
    val rotationSensitivity = ConfigDouble("rotationSensitivity", 1.0, 0.1, 10.0, "Sensitivity of the scroll wheel")
    val scaleSensitivity = ConfigDouble("scaleSensitivity", 1.0, 0.1, 10.0, "Sensitivity of the scroll wheel")
    override fun include(): Boolean {
        return FabricLoader.getInstance().isModLoaded("isometric-renders")
    }
}