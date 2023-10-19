package dev.kikugie.techutils.client.feature.preview

import dev.kikugie.techutils.config.ConfigGroup
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.options.ConfigBoolean
import fi.dy.masa.malilib.config.options.ConfigDouble
import fi.dy.masa.malilib.config.options.ConfigInteger

object PreviewConfig : ConfigGroup {
    @JvmField
    val customMetadata = ConfigBoolean(
        "customMetadata", true,
        "Show custom metadata widget in Load Schematics menu"
    )

    @JvmField
    val renderPreview = ConfigBoolean(
        "renderPreview", true,
        "Show 3D render of selected litematic in Load Schematics menu\n(Works only for .litematic files)"
    )

    @JvmField
    val overridePreview = ConfigBoolean(
        "overridePreview", false,
        "Show 3D render even if litematic has its own preview"
    )
    val previewAngle = ConfigInteger("previewAngle", 45, 0, 360, "Angle of the preview")
    val previewSlant = ConfigInteger("previewSlant", 30, 0, 90, "Slant of the preview")
    val rotationSensitivity = ConfigDouble("rotationSensitivity", 1.0, 0.1, 10.0, "Sensitivity of the scroll wheel")
    val scaleSensitivity = ConfigDouble("scaleSensitivity", 1.0, 0.1, 10.0, "Sensitivity of the scroll wheel")

    override fun getConfigs(): Collection<IConfigBase> {
        return listOf(
            renderPreview,
            overridePreview,
            previewAngle,
            previewSlant,
            rotationSensitivity,
            scaleSensitivity
        )
    }
}