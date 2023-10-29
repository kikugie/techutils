package dev.kikugie.techutils.client.feature.misc

import dev.kikugie.techutils.client.config.annotation.Group
import dev.kikugie.techutils.client.config.option.Options
import dev.kikugie.techutils.client.feature.misc.impl.GiveFullIInv
import dev.kikugie.techutils.client.gui.config.TechUtilsConfigGui
import net.minecraft.client.MinecraftClient

@Group(Group.MISC)
object MiscConfig {
    val openConfig = Options.create("openConfig", "X, O") {
        MinecraftClient.getInstance().setScreen(TechUtilsConfigGui(null))
        return@create true
    }
    val giveInvHotkey = Options.create("giveFullInv", "") {
        GiveFullIInv.onKeybind()
    }
    val bundleFill = Options.create("bundleFill", 1, 1, 100)
    val fillSafety = Options.create("fillSafety")
}