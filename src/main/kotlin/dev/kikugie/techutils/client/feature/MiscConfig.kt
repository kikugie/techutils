package dev.kikugie.techutils.client.feature

import dev.kikugie.techutils.client.config.TechUtilsConfigGui
import dev.kikugie.techutils.client.config.annotation.Group
import dev.kikugie.techutils.client.config.option.Options.create
import dev.kikugie.techutils.client.feature.util.GiveFullIInv
import net.minecraft.client.MinecraftClient

@Group(Group.MISC)
object MiscConfig {
    val openConfig = create("openConfig", "X, O") {
        MinecraftClient.getInstance().setScreen(TechUtilsConfigGui(null))
        return@create true
    }
    val compactScoreboard = create("compactScoreboard", false, "F6")
    val giveInvHotkey = create("giveFullInv", "") {
        GiveFullIInv.onKeybind()
    }
    val bundleFill = create("bundleFill", 1, 1, 100)
    val fillSafety = create("fillSafety")
    val easyPlaceFullBlocks = create("easyPlaceFullBlocks", false, "")
}