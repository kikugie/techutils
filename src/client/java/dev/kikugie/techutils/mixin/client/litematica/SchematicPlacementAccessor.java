package dev.kikugie.techutils.mixin.client.litematica;

import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(value = SchematicPlacement.class, remap = false)
public interface SchematicPlacementAccessor {
    @Invoker("onModified")
    void invokeOnModified(@Nullable String regionName, SchematicPlacementManager manager);
}
