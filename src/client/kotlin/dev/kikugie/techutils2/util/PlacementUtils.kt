package dev.kikugie.techutils2.util

import fi.dy.masa.litematica.data.DataManager

object PlacementUtils {
    val selectedPlacement
        get() = DataManager.getSchematicPlacementManager().selectedSchematicPlacement
}