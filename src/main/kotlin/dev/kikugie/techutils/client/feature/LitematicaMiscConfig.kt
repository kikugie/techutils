package dev.kikugie.techutils.client.feature

import dev.kikugie.techutils.client.config.annotation.Group
import dev.kikugie.techutils.client.config.option.Options.create
import dev.kikugie.techutils.client.feature.downloader.DownloaderGui
import dev.kikugie.techutils.client.util.litematica.InGameNotifier
import dev.kikugie.techutils.client.util.litematica.PlacementUtils
import dev.kikugie.techutils.client.util.multiversion.floor
import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.data.SchematicHolder
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.malilib.gui.GuiBase
import fi.dy.masa.malilib.gui.Message
import net.minecraft.client.MinecraftClient
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos

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
        GuiBase.openGui(DownloaderGui { file ->
            // TODO: Use Structure.load() instead
            val schematic = LitematicaSchematic.createFromFile(file.parentFile, file.name)
            if (schematic == null) {
                InGameNotifier.addMessage(Message.MessageType.ERROR, "techutils.downloader.failLoad")
                return@DownloaderGui
            }
            SchematicHolder.getInstance().addSchematic(schematic, true)
            if (DataManager.getCreatePlacementOnLoad()) {
                val pos = MinecraftClient.getInstance().player?.pos?.floor() ?: BlockPos.ORIGIN
                val name = schematic.metadata.name
                val manager = DataManager.getSchematicPlacementManager()
                val placement = SchematicPlacement.createFor(schematic, pos, name, true, true)
                manager.addSchematicPlacement(placement, true)
                manager.selectedSchematicPlacement = placement
            }
        })
        true
    }
}