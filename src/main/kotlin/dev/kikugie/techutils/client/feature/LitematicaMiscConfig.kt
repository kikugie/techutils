package dev.kikugie.techutils.client.feature

import dev.kikugie.techutils.client.config.annotation.Group
import dev.kikugie.techutils.client.config.option.Options.create
import dev.kikugie.techutils.client.feature.downloader.DownloaderGui
import dev.kikugie.techutils.client.util.litematica.InGameNotifier
import dev.kikugie.techutils.client.util.litematica.PlacementUtils
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.malilib.gui.GuiBase
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation

@Group(Group.LITEMATICA)
object LitematicaMiscConfig {
    val rotatePlacement = create("rotatePlacement", "M, R") {
        val placement = PlacementUtils.selectedPlacement ?: return@create false
        placement.setRotation(placement.rotation.rotate(BlockRotation.CLOCKWISE_90), InGameNotifier)
        return@create true
    }
    val mirrorPlacement = create("mirrorPlacement", "M, T") {
        val placement = PlacementUtils.selectedPlacement ?: return@create false
        val mirror = when (placement.mirror!!) {
            BlockMirror.NONE -> BlockMirror.LEFT_RIGHT
            BlockMirror.LEFT_RIGHT -> BlockMirror.FRONT_BACK
            BlockMirror.FRONT_BACK -> BlockMirror.NONE
        }
        placement.setMirror(mirror, InGameNotifier)
        return@create true
    }
    val refreshMaterialList = create("refreshMaterialList", "") {
        DataManager.getMaterialList()?.reCreateMaterialList() ?: return@create false
        return@create true
    }
    val downloadSchematic = create("downloadSchematic", "") {
        GuiBase.openGui(DownloaderGui())
        true
    }
}