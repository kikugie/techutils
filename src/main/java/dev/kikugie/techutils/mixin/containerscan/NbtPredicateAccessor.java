package dev.kikugie.techutils.mixin.containerscan;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.NbtPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NbtPredicate.class)
public interface NbtPredicateAccessor {
	@Accessor("nbt")
	NbtCompound nbt();
}
