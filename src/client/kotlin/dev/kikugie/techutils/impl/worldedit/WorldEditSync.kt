package dev.kikugie.techutils.impl.worldedit

import dev.kikugie.techutils.config.WorldEditConfig
import dev.kikugie.techutils.util.InGameNotifier
import dev.kikugie.techutils.util.PlacementUtils
import fi.dy.masa.litematica.selection.Box
import fi.dy.masa.malilib.gui.Message
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockBox

object WorldEditSync {
    val client = MinecraftClient.getInstance()
    val handler
        get() = WorldEditNetworkHandler.INSTANCE
    val queue = MuteQueue()
    var tick = 0
    var lastBox: BlockBox? = null

    private val REGEX1 = Regex("Side effect \"Neighbors\".+")
    private val REGEX2 = Regex("(\\w+ position set to \\(.+\\).)|(Position already set.)")

    fun onConnected() {
        if (WorldEditConfig.disableUpdates.booleanValue) disableNeighborUpdates()
    }

    fun tick() {
        if (handler?.shouldTick == true && WorldEditConfig.sync.booleanValue) syncSelection()
    }

    private fun disableNeighborUpdates() {
        queue += REGEX1
        client.networkHandler?.sendCommand("/perf neighbors off")
    }

    private fun syncSelection() {
        if (tick > 0) tick--
        if (handler?.storage?.cuboid != true) return
        val box = PlacementUtils.selection?.selectedSubRegionBox?.toIntBox()
            ?: return
        if (box != lastBox) {
            lastBox = box
            tick = WorldEditConfig.syncTicks.integerValue
            return
        }

        if (tick != 0) return
        tick = -1
        updateRegion(box)
        if (WorldEditConfig.syncFeedback.booleanValue)
            InGameNotifier.addMessage(Message.MessageType.SUCCESS, "techutils.worldedit_sync.synced")
    }

    private fun updateRegion(box: BlockBox)  {
        queue += REGEX2
        client.networkHandler?.sendCommand("/pos1 ${box.minX},${box.minY},${box.minZ}")
        queue += REGEX2
        client.networkHandler?.sendCommand("/pos2 ${box.maxX},${box.maxY},${box.maxZ}")
    }
}

private val Box.isValid
    get() = pos1 != null && pos2 != null

private fun Box.toIntBox() = if (isValid) BlockBox.create(pos1, pos2) else null