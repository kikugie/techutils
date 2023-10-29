package dev.kikugie.techutils.mixin.mod.litematica.placement;

import dev.kikugie.techutils.access.PlacementListener;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(value = SchematicPlacement.class, remap = false)
public class SchematicPlacementMixin implements PlacementListener {
    @Unique
    private final List<Consumer<SchematicPlacement>> listeners = new ArrayList<>();
    @Inject(method = {"onModified(Lfi/dy/masa/litematica/schematic/placement/SchematicPlacementManager;)V", "onModified(Ljava/lang/String;Lfi/dy/masa/litematica/schematic/placement/SchematicPlacementManager;)V"}, at = @At("HEAD"))
    private void invokeListeners(CallbackInfo ci) {
        for (Consumer<SchematicPlacement> lis : this.listeners)
            lis.accept((SchematicPlacement) (Object) this);
    }

    @Override
    public void registerListener(Consumer<SchematicPlacement> onModified) {
        this.listeners.add(onModified);
    }
}
