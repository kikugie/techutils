package dev.kikugie.techutils.config

import dev.kikugie.malilib_extras.api.config.Category
import dev.kikugie.techutils.util.InGameNotifier
import dev.kikugie.techutils.gui.DownloaderGui
import dev.kikugie.techutils.util.PlacementUtils
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed
import fi.dy.masa.malilib.config.options.ConfigHotkey
import fi.dy.masa.malilib.gui.GuiBase
import net.minecraft.util.BlockMirror.*
import net.minecraft.util.BlockRotation

@Suppress("unused")
@Category(LITEMATICA)
object LitematicaMisc {
    val rotatePlacement = FACTORY.create<ConfigHotkey>("rotatePlacement", "M, R") {
        callback = {
            val placement = PlacementUtils.selectedPlacement
            placement?.setRotation(placement.rotation.rotate(BlockRotation.CLOCKWISE_90), InGameNotifier)
            true
        }
    }
    val mirrorPlacement = FACTORY.create<ConfigHotkey>("mirrorPlacement", "M, T") {
        callback = {
            val placement = PlacementUtils.selectedPlacement
            val mirror = when (placement?.mirror!!) {
                NONE -> LEFT_RIGHT
                LEFT_RIGHT -> FRONT_BACK
                FRONT_BACK -> NONE
            }
            placement.setMirror(mirror, InGameNotifier)
            true
        }
    }
    val refreshMaterialList = FACTORY.create<ConfigHotkey>("refreshMaterialList", "") {
        callback = {
            DataManager.getMaterialList()?.reCreateMaterialList()
            true
        }
    }
    val downloadSchematic = FACTORY.create<ConfigHotkey>("downloadSchematic", "K, D") {
        callback = {
            GuiBase.openGui(DownloaderGui())
            true
        }
    }
    val easyPlaceFullBlocks = FACTORY.create<ConfigBooleanHotkeyed>("easyPlaceFullBlocks", false, "") {}
}