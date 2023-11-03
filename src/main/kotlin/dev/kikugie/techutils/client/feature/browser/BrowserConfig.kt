package dev.kikugie.techutils.client.feature.browser

import dev.kikugie.techutils.client.config.annotation.Group
import dev.kikugie.techutils.client.config.option.Options.create

@Group(Group.LITEMATICA)
object BrowserConfig {
    val improvedBrowser = create("improvedBrowser")
    val previewAngle = create("previewAngle", 45, 0, 360)
    val previewSlant = create("previewSlant", 30, 0, 90)
    val scrollSensitivity = create("scrollSensitivity", 1.0, 0.1, 10.0, true)
}