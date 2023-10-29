package dev.kikugie.techutils.client.feature.containerscan.storage

import dev.kikugie.techutils.access.PlacementListener
import dev.kikugie.techutils.client.util.litematica.PlacementUtils.PlacementPos
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.inventory.Inventory
import net.minecraft.util.math.BlockPos

class InventoryWorld(val placement: SchematicPlacement) {
    val placementInventories = Long2ObjectOpenHashMap<Inventory?>()
    val realInventories = Long2ObjectOpenHashMap<Inventory?>()
    private val schematic: LitematicaSchematic
        get() = placement.schematic

    init {
        (placement as PlacementListener).registerListener { clearInventories() }
    }

    private fun clearInventories() {
        placementInventories.clear()
        realInventories.clear()
    }

    fun populatePlacement(pos: PlacementPos) {
        val posl = pos.world.asLong()
        if (!placementInventories.contains(posl))
            placementInventories[posl] = pos.inventory
    }

    fun populateReal(pos: BlockPos, inventory: Inventory?) {
        realInventories[pos.asLong()] = inventory
    }

    operator fun get(pos: BlockPos): LinkedInventory {
        val posl = pos.asLong()
        return LinkedInventory(placementInventories[posl], realInventories[posl])
    }
}