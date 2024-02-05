package dev.kikugie.techutils.util

import fi.dy.masa.litematica.data.DataManager

object PlacementUtils {
    val selectedPlacement
        get() = DataManager.getSchematicPlacementManager().selectedSchematicPlacement
    val selection
        get() = DataManager.getSelectionManager().currentSelection
}