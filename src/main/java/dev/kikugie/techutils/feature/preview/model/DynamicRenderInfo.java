package dev.kikugie.techutils.feature.preview.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class DynamicRenderInfo {

	public static DynamicRenderInfo EMPTY = new DynamicRenderInfo(ImmutableMap.of(), ImmutableMultimap.of());

	protected Map<BlockPos, BlockEntity> blockEntities;
	protected Multimap<Vec3d, EntityEntry> entities;

	public DynamicRenderInfo(Map<BlockPos, BlockEntity> blockEntities, Multimap<Vec3d, EntityEntry> entities) {
		this.blockEntities = ImmutableMap.copyOf(blockEntities);
		this.entities = ImmutableMultimap.copyOf(entities);
	}

	public Map<BlockPos, BlockEntity> blockEntities() {
		return this.blockEntities;
	}

	public Multimap<Vec3d, EntityEntry> entities() {
		return this.entities;
	}

	public boolean isEmpty() {
		return this.blockEntities.isEmpty() && this.entities.isEmpty();
	}

	public record EntityEntry(Entity entity, int light) {}

}
