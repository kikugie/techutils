package dev.kikugie.techutils.client.feature.containerscan.storage

import net.minecraft.inventory.Inventory

data class LinkedInventory(
    val placementInventory: Inventory?,
    val realInventory: Inventory?
) {
    val linked: Boolean
        get() = placementInventory != null && realInventory != null
}