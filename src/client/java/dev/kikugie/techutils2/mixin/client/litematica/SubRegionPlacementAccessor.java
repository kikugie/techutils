package dev.kikugie.techutils2.mixin.client.litematica;

import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(value = SubRegionPlacement.class, remap = false)
public interface SubRegionPlacementAccessor {
    @Invoker("setEnabled")
    void forceSetEnabled(boolean val);

    @Invoker("setPos")
    void forceSetPos(BlockPos pos);

    @Invoker("setRotation")
    void forceSetRotation(BlockRotation pos);

    @Invoker("setMirror")
    void forceSetMirror(BlockMirror pos);

    @Accessor("ignoreEntities")
    void forceSetIgnoreEntities(boolean val);
}
