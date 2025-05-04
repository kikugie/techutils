package dev.kikugie.techutils.mixin.mod.litematica;

import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = LitematicaSchematic.class, remap = false)
public interface LitematicaSchematicAccessor {
	@Accessor("blockContainers")
	Map<String, LitematicaBlockStateContainer> getBlockContainers();

	@Accessor("tileEntities")
	Map<String, Map<BlockPos, NbtCompound>> getTileEntities();

	@Mutable
	@Accessor("tileEntities")
	void setTileEntities(Map<String, Map<BlockPos, NbtCompound>> tileEntities);
}
