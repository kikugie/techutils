package dev.kikugie.techutils.client.feature.containerscan

import dev.kikugie.techutils.client.feature.containerscan.screen.CatchingScreenHandler
import dev.kikugie.techutils.client.feature.containerscan.screen.ScreenBlocker
import dev.kikugie.techutils.client.feature.containerscan.storage.InventoryWorld
import dev.kikugie.techutils.client.task.AwaitTask
import dev.kikugie.techutils.client.task.TaskManager
import dev.kikugie.techutils.client.util.litematica.PlacementUtils
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider
import net.minecraft.inventory.Inventory
import net.minecraft.util.hit.BlockHitResult
import java.util.*

object InteractionHandler {
    val fillContainers
        get() = ContainerScanConfig.fillContainers.booleanValue
    val scanContainers
        get() = ContainerScanConfig.scanContainers.booleanValue
    val worlds = WeakHashMap<SchematicPlacement, InventoryWorld>()

    fun onInteraction(hit: BlockHitResult) {
        val player = MinecraftClient.getInstance().player!!
        val pos = PlacementUtils.getPlacementPos(hit.blockPos) ?: return
        val world = worlds.computeIfAbsent(pos.placement) { InventoryWorld(it) }
        world.populatePlacement(pos)

        val task = AwaitTask<Inventory> { world.populateReal(hit.blockPos, it) }
        TaskManager.add(task)
        ScreenBlocker.add { screen ->
            val provider = screen as? ScreenHandlerProvider<*> ?: return@add ScreenBlocker.ActionResult.FAIL
            player.currentScreenHandler = CatchingScreenHandler(provider.screenHandler) {
                task.complete(it)
                MinecraftClient.getInstance().player!!.closeHandledScreen()
            }
            return@add ScreenBlocker.ActionResult.BLOCK
        }
    }


}