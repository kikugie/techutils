package dev.kikugie.techutils.util;

import dev.kikugie.techutils.mixin.containerscan.EnchantmentPredicateAccessor;
import dev.kikugie.techutils.mixin.containerscan.NbtPredicateAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ItemPredicateUtils {
	public static final String PREDICATE_ID = "techutils:item_predicate";
	public static final String PLACEHOLDER_ID = "techutils:placeholder";
	private static final Map<String, ItemPredicate> PREDICATE_CACHE = new HashMap<>();
	private static final Reference2ReferenceOpenHashMap<ItemPredicate, List<Text>> PRETTIFIED_PREDICATES = new Reference2ReferenceOpenHashMap<>();

	private ItemPredicateUtils() {}

	public static ItemStack createPredicateStack(String rawPredicate, ItemStack placeholder) {
		var nbt = new NbtCompound();
		ItemStack stack = Items.COMMAND_BLOCK.getDefaultStack();

		nbt.putString("Command", rawPredicate);
		BlockItem.setBlockEntityNbt(stack, BlockEntityType.COMMAND_BLOCK, nbt);

		setPlaceholder(stack, placeholder);

		stack.getOrCreateNbt().put(PREDICATE_ID, new NbtCompound());

		stack.setCustomName(Text.literal("Item Predicate")
			.styled(style -> style.withColor(Formatting.WHITE).withItalic(false))
		);

		return stack;
	}

	public static boolean isPredicate(ItemStack stack) {
		return stack.getItem() == Items.COMMAND_BLOCK
			&& stack.getNbt() != null
			&& stack.getNbt().contains(PREDICATE_ID);
	}

	public static String getRawPredicate(ItemStack stack) {
		return stack.getNbt() != null
			? stack.getNbt().getCompound("BlockEntityTag").getString("Command")
			: "";
	}

	public static @Nullable ItemPredicate getPredicate(ItemStack stack) {
		if (!isPredicate(stack)) {
			return null;
		}

		var rawPredicate = getRawPredicate(stack);
		return getPredicate(rawPredicate);
	}

	public static ItemPredicate getPredicate(String rawPredicate) {
		if (PREDICATE_CACHE.containsKey(rawPredicate)) {
			return PREDICATE_CACHE.get(rawPredicate);
		}

		int startingTokenIndex = rawPredicate.indexOf('{');
		if (startingTokenIndex == -1)
			return saveFailedPredicate(rawPredicate, "No item predicate is present!");

		rawPredicate = rawPredicate.substring(startingTokenIndex);

		NbtCompound nbt;
		try {
			nbt = StringNbtReader.parse(rawPredicate).getCompound("predicate");
			if (nbt.isEmpty()) {
				throw new IllegalArgumentException("No item predicate is present!");
			}
		} catch (Throwable throwable) {
			return saveFailedPredicate(rawPredicate, throwable.getMessage());
		}

		var result = ItemPredicate.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE, MinecraftClient.getInstance().world.getRegistryManager()), nbt);
		if (result.result().isPresent()) {
			var predicate = result.result().get();
			PREDICATE_CACHE.put(rawPredicate, predicate);
			PRETTIFIED_PREDICATES.put(predicate, ContainerUtils.prettifyNbt(nbt));

			return predicate;
		} else {
			return saveFailedPredicate(rawPredicate, result.error().get().message());
		}
	}

	public static List<Text> getPrettyPredicate(ItemStack predicateStack) {
		var predicate = ItemPredicateUtils.getPredicate(predicateStack);
		if (predicate == null) {
			return List.of();
		}

		var placeholder = ItemPredicateUtils.getPlaceholder(predicateStack);
		if (placeholder != null) {
			var nbt = new NbtCompound();
			nbt.put("placeholder", placeholder.writeNbt(new NbtCompound()));
			var lines = new ArrayList<>(PRETTIFIED_PREDICATES.get(predicate));
			lines.addAll(ContainerUtils.prettifyNbt(nbt));
			return lines;
		} else {
			return Collections.unmodifiableList(PRETTIFIED_PREDICATES.get(predicate));
		}
	}

	public static List<Text> getErrorLines(ItemStack stack, ItemPredicate predicate) {
		var lines = new ArrayList<Text>();
		var items = predicate.items();
		var tag = predicate.tag();
		var count = predicate.count();
		var durability = predicate.durability();
		var enchantments = predicate.enchantments();
		var storedEnchantments = predicate.storedEnchantments();
		var potion = predicate.potion();
		var nbt = predicate.nbt();

		if (tag.isPresent() && !stack.isIn(tag.get())) {
			TagKey<Item> tagKey = tag.orElseThrow();

			var msg = Text.literal("Incorrect item type. Expected tag '%s' with items: ".formatted(tagKey.id()))
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			Registries.ITEM.getEntryList(tagKey).ifPresent(el -> el.stream()
				.flatMap(i -> Stream.of(Text.of(", "), Text.of(Registries.ITEM.getId(i.value()).toString())))
				.skip(1)
				.forEach(msg::append));
			lines.add(msg);
		}

		if (items.isPresent() && !items.get().contains(stack.getItem().getRegistryEntry())) {
			RegistryEntryList<Item> itemsList = items.orElseThrow();

			var msg = Text.literal("Incorrect item type. Expected: ")
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			itemsList.stream()
				.flatMap(i -> Stream.of(Text.of(", "), Text.of(Registries.ITEM.get(i.getKey().orElseThrow()).toString())))
				.skip(1)
				.forEach(msg::append);
			lines.add(msg);
		}

		Function<NumberRange.IntRange, String> intRangeToString = range -> {
			StringBuilder sb = new StringBuilder();
			var min = range.min();
			var max = range.max();
			if (min != null && min.equals(max)) {
				sb.append(min);
			} else {
				if (min != null) {
					sb.append("at least ").append(min);
					if (max != null) {
						sb.append(" and ");
					}
				}
				if (max != null) {
					sb.append("at most ").append(max);
				}
			}
			return sb.toString();
		};

		if (!count.test(stack.getCount())) {
			var msg = Text.literal("Incorrect count. Expected: ")
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			msg.append(intRangeToString.apply(count));
			lines.add(msg);
		}

		if (!durability.isDummy()) {
			if (!stack.isDamageable()) {
				var msg = Text.literal("The item should be damageable!")
					.styled(style -> style.withColor(Formatting.RED).withItalic(false));
				lines.add(msg);
			} else if (!durability.test(stack.getMaxDamage() - stack.getDamage())) {
				var msg = Text.literal("Incorrect durability. Expected: ")
					.styled(style -> style.withColor(Formatting.RED).withItalic(false));
				msg.append(intRangeToString.apply(durability));
				lines.add(msg);
			}
		}

		var unsatisfiedEnchantments = new ArrayList<Text>();
		var enchantmentNbt =
			enchantments.size() > 0
				? stack.getEnchantments()
				: (storedEnchantments.size() > 0 ? EnchantedBookItem.getEnchantmentNbt(stack) : null);
		if (enchantmentNbt != null) {
			Map<Enchantment, Integer> enchantmentLevels = EnchantmentHelper.fromNbt(enchantmentNbt);

			for (var enchantmentPredicate : enchantments) {
				if (enchantmentPredicate.enchantment().isEmpty()) continue;

				if (!enchantmentPredicate.test(enchantmentLevels)) {
					unsatisfiedEnchantments.add(
						Text.literal("'%s' with level %s"
							.formatted(Registries.ENCHANTMENT.get(enchantmentPredicate.enchantment().orElseThrow().getKey().orElseThrow()),
								intRangeToString.apply(enchantmentPredicate.levels())
							)
						)
					);
				}
			}
		}
		if (!unsatisfiedEnchantments.isEmpty()) {
			var msg = Text.literal("Incorrect enchantments. Expected the following enchantments:")
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			lines.add(msg);
			lines.addAll(unsatisfiedEnchantments);
		}

		if (potion.isPresent() && potion.get() != PotionUtil.getPotion(stack)) {
			RegistryEntry<Potion> potionRegistryEntry = potion.orElseThrow();

			var msg = Text.literal("Incorrect potion. Expected '%s'".formatted(Registries.POTION.get(potionRegistryEntry.getKey().orElseThrow())))
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			lines.add(msg);
		}

		if (!nbt.get().test(stack)) {
			var msg = Text.literal("Incorrect NBT. Expected:")
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			lines.add(msg);
			lines.addAll(ContainerUtils.prettifyNbt(nbt.get().nbt()));
		}

		return lines;
	}

	@Nullable
	public static ItemStack getPlaceholder(ItemStack stack) {
		var nbt = stack.getNbt();
		return isPredicate(stack) && nbt != null && nbt.contains(PLACEHOLDER_ID)
			? ItemStack.fromNbt(nbt.getCompound(PLACEHOLDER_ID))
			: null;
	}

	public static void setPlaceholder(ItemStack predicateStack, ItemStack placeholder) {
		if (placeholder == null || placeholder.isEmpty()) {
			if (predicateStack.getNbt() != null) {
				predicateStack.getNbt().remove(PLACEHOLDER_ID);
			}
		} else {
			predicateStack.getOrCreateNbt().put(PLACEHOLDER_ID, placeholder.writeNbt(new NbtCompound()));
		}
	}

	private static ItemPredicate saveFailedPredicate(String rawPredicate, String message) {
		var markerPredicate = ItemPredicate.Builder.create().count(NumberRange.IntRange.exactly(-1)).build();
		PREDICATE_CACHE.put(rawPredicate, markerPredicate);

		var title = Text.literal("Could not parse item predicate!")
			.styled(style -> style.withColor(Formatting.RED).withItalic(false));
		var lines = new ArrayList<Text>();
		lines.add(title);
		for (String line : message.split("\n")) {
			lines.add(Text.literal(line)
				.styled(style -> style.withColor(Formatting.RED).withItalic(false)));
		}
		PRETTIFIED_PREDICATES.put(markerPredicate, lines);
		return markerPredicate;
	}
}
