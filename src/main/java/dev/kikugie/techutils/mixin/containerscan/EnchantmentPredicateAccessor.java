package dev.kikugie.techutils.mixin.containerscan;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(EnchantmentPredicate.class)
public interface EnchantmentPredicateAccessor {
	@Accessor("enchantment")
	Optional<RegistryEntry<Enchantment>> enchantment();

	@Accessor("levels")
	NumberRange.IntRange levels();
}
