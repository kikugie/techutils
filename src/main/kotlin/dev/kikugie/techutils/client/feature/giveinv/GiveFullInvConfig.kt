package dev.kikugie.techutils.client.feature.giveinv

import dev.kikugie.techutils.config.ConfigGroup
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.options.ConfigBoolean
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.config.options.ConfigInteger

object GiveFullInvConfig : ConfigGroup {
    val giveInvHotkey = ConfigHotkey(
        "giveFullInv", "G", """
            Give full inventory of an item with following rules:
            - Main hand: item; Off hand: none; Result: shulker box of item.
            - Main hand: item; Off hand: container; Result: container of item.
            - Main hand: item; Off hand: bundle; Result: bundle of item * config.
            - Main hand: empty shulker box; Off hand: none: Result: chest of stacked boxes.
            - Main hand: empty shulker box; Off hand: container; Result: container of stacked boxes.
            - Main hand: shulker box; Off hand: none: Result: chest of full boxes.
            - Main hand: shulker box; Off hand: container; Result: container of full boxes.
            
            """.trimIndent()
    )
    val bundleFill = ConfigInteger(
        "bundleFill", 1, 1, 100, true,
        "Amount of stacks to put in a bundle when using giveFullInv feature"
    )
    val fillSafety = ConfigBoolean(
        "fillSafety", true, """
            Restrict nested containers to prevent crashing yourself.
            Here be cats and scratches!
            """.trimIndent()
    )

    override fun getConfigs(): Collection<IConfigBase> {
        return listOf(giveInvHotkey, bundleFill, fillSafety)
    }
}