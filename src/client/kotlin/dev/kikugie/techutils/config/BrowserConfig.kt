package dev.kikugie.techutils.config

import dev.kikugie.malilib_extras.api.config.Category
import fi.dy.masa.malilib.config.options.*

@Category(LITEMATICA)
object BrowserConfig {
    val improvedBrowser = FACTORY.create<ConfigBoolean>("improvedBrowser", true)
    val previewAngle = FACTORY.create<ConfigInteger>("previewAngle", 45) {
        min = 0
        max = 360
        slider = true
    }
    val previewSlant = FACTORY.create<ConfigInteger>("previewSlant", 30) {
        min = 0
        max = 90
        slider = true
    }
    val mouseSensitivity = FACTORY.create<ConfigDouble>("mouseSensitivity", 1.0) {
        min = 0.1
        max = 10.0
        slider = true
    }
}