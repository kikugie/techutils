package dev.kikugie.techutils.client.feature.browser

import dev.kikugie.techutils.client.config.annotation.Group
import dev.kikugie.techutils.client.config.option.Options.create

@Group(Group.LITEMATICA)
object BrowserConfig {
    val improvedBrowser = create("improvedBrowser")
}