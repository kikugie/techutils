package me.kikugie.techutils.feature

import fi.dy.masa.litematica.data.DataManager
import fi.dy.masa.litematica.selection.AreaSelection
import me.kikugie.techutils.TechUtilsMod
import me.kikugie.techutils.config.Configs
import me.kikugie.techutils.networking.WorldEditNetworkHandler
import me.kikugie.techutils.utils.ResponseMuffler
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.Box

class WorldEditActionManager private constructor() {
    private val handler: WorldEditNetworkHandler = WorldEditNetworkHandler.init()!!
    private val client = MinecraftClient.getInstance()
    private var lastBox: Box? = null
    private var syncTimer = -1
    private var initComplete = false
    fun onWorldEditConnected() {
        ClientTickEvents.START_WORLD_TICK.register(ClientTickEvents.StartWorldTick register@{
            if (!client.player!!.hasPermissionLevel(2)) return@register
            if (Configs.WorldEditConfigs.AUTO_DISABLE_UPDATES.booleanValue) disableNeighborUpdates()
            if (Configs.WorldEditConfigs.AUTO_WE_SYNC.booleanValue) syncSelection()
            if (!initComplete) initComplete = true
        })
    }

    private fun disableNeighborUpdates() {
        if (initComplete) return
        ResponseMuffler.scheduleMute("Side effect \"Neighbors\".+")
        client.networkHandler!!.sendCommand("/perf neighbors off")
        TechUtilsMod.LOGGER.debug("Turning off perf")
    }

    private fun syncSelection() {
        if (syncTimer > 0) syncTimer--
        if (!handler.storage.isCuboidMode) return
        val box = validActiveSelection ?: return
        val mathBox = Box(box.pos1, box.pos2)
        if (mathBox != lastBox) {
            lastBox = mathBox
            syncTimer = Configs.WorldEditConfigs.AUTO_WE_SYNC_TICKS.integerValue
            return
        }
        if (syncTimer != 0) return
        syncTimer = -1
        updateRegion(box)
        TechUtilsMod.LOGGER.debug("WorldEdit synced!")
        client.player!!.sendMessage(Text.translatable("techutils.feedback.wesync.success"), true)
    }

    private fun updateRegion(box: fi.dy.masa.litematica.selection.Box) {
        ResponseMuffler.scheduleMute("(\\w+ position set to \\(.+\\).)|(Position already set.)")
        client.networkHandler!!.sendCommand(
            String.format(
                "/pos1 %d,%d,%d",
                box.pos1!!.x,
                box.pos1!!.y,
                box.pos1!!.z
            )
        )
        ResponseMuffler.scheduleMute("(\\w+ position set to \\(.+\\).)|(Position already set.)")
        client.networkHandler!!.sendCommand(
            String.format(
                "/pos2 %d,%d,%d",
                box.pos2!!.x,
                box.pos2!!.y,
                box.pos2!!.z
            )
        )
    }

    private val validActiveSelection: fi.dy.masa.litematica.selection.Box?
        get() {
            val selection: AreaSelection = DataManager.getSelectionManager().currentSelection ?: return null
            val box: fi.dy.masa.litematica.selection.Box? = selection.selectedSubRegionBox
            if (box != null) {
                return if (box.pos1 != null && box.pos2 != null) box else null
            }
            return null
        }

    companion object {
        private var instance: WorldEditActionManager? = null

        fun getInstance(): WorldEditActionManager? {
            return instance
        }

        fun init(): WorldEditActionManager {
            instance = WorldEditActionManager()
            return instance as WorldEditActionManager
        }

        fun reset() {
            instance = null
        }
    }
}