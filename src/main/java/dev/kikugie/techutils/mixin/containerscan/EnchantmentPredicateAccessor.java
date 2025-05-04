package dev.kikugie.techutils.mixin.containerscan;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.EnchantmentPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnchantmentPredicate.class)
public interface EnchantmentPredicateAccessor {
	@Accessor("enchantment")
	Enchantment enchantment();

	@Accessor("levels")
	NumberRange.IntRange levels();
}
