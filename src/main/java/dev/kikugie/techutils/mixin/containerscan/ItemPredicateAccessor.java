package dev.kikugie.techutils.mixin.containerscan;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(ItemPredicate.class)
public interface ItemPredicateAccessor {
	@Accessor("tag")
	@Nullable Optional<TagKey<Item>> tag();

	@Accessor("items")
	@Nullable Optional<Set<Item>> items();

	@Accessor("count")
	NumberRange.IntRange count();

	@Accessor("durability")
	NumberRange.IntRange durability();

	@Accessor("enchantments")
	List<EnchantmentPredicate> enchantments();

	@Accessor("storedEnchantments")
	List<EnchantmentPredicate> storedEnchantments();

	@Accessor("potion")
	@Nullable Optional<RegistryEntry<Potion>> potion();

	@Accessor("nbt")
	Optional<NbtPredicate> nbt();
}
