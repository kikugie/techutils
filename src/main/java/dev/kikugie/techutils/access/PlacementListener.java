package dev.kikugie.techutils.access;

import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;

import java.util.function.Consumer;

public interface PlacementListener {
    void registerListener(Consumer<SchematicPlacement> listener);
}
