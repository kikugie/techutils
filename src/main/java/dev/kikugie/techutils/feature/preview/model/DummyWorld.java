package dev.kikugie.techutils.feature.preview.model;

import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.dimension.DimensionType;

import java.util.function.Supplier;

public class DummyWorld extends WorldSchematic {
	protected DummyWorld(MutableWorldProperties properties, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler) {
		super(properties, registryManager, dimensionEntry, profiler, null);
	}

	public static DummyWorld fromWorld(ClientWorld world) {
		MutableWorldProperties properties = world.getLevelProperties();
		DynamicRegistryManager registryManager = world.getRegistryManager();
		RegistryEntry<DimensionType> dimensionEntry = world.getDimensionEntry();
		Supplier<Profiler> profiler = world.getProfilerSupplier();
		return new DummyWorld(properties, registryManager, dimensionEntry, profiler);
	}
}
