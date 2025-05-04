package dev.kikugie.techutils.mixin.containerscan;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ItemPredicate.class)
public interface ItemPredicateAccessor {
	@Accessor("tag")
	@Nullable TagKey<Item> tag();

	@Accessor("items")
	@Nullable Set<Item> items();

	@Accessor("count")
	NumberRange.IntRange count();

	@Accessor("durability")
	NumberRange.IntRange durability();

	@Accessor("enchantments")
	EnchantmentPredicate[] enchantments();

	@Accessor("storedEnchantments")
	EnchantmentPredicate[] storedEnchantments();

	@Accessor("potion")
	@Nullable Potion potion();

	@Accessor("nbt")
	NbtPredicate nbt();
}
