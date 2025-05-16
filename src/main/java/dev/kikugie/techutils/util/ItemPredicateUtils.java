package dev.kikugie.techutils.util;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class ItemPredicateUtils {
	public static final String PREDICATE_ID = "techutils:item_predicate";
	private static final Map<String, ItemPredicate> PREDICATE_CACHE = new HashMap<>();
	private static final Reference2ReferenceOpenHashMap<ItemPredicate, List<Text>> PRETTIFIED_PREDICATES = new Reference2ReferenceOpenHashMap<>();

	private ItemPredicateUtils() {}

	public static ItemStack createPredicateStack(String rawPredicate, ItemStack placeholder) {
		var nbt = new NbtCompound();
		ItemStack stack = Items.COMMAND_BLOCK.getDefaultStack();

		nbt.putString("Command", rawPredicate);
		BlockItem.setBlockEntityData(stack, BlockEntityType.COMMAND_BLOCK, nbt);

		setPlaceholder(stack, placeholder);

		stack.apply(
			DataComponentTypes.CUSTOM_DATA,
			NbtComponent.DEFAULT,
			nbtComponent -> nbtComponent.apply(custom -> custom.put(PREDICATE_ID, new NbtCompound()))
		);

		stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Item Predicate")
			.styled(style -> style.withColor(Formatting.WHITE).withItalic(false))
		);

		return stack;
	}

	public static boolean isPredicate(ItemStack stack) {
		return stack.getItem() == Items.COMMAND_BLOCK
			&& stack.get(DataComponentTypes.CUSTOM_DATA) instanceof NbtComponent nbtComponent
			&& nbtComponent.getNbt().contains(PREDICATE_ID);
	}

	public static String getRawPredicate(ItemStack stack) {
		return stack.get(DataComponentTypes.BLOCK_ENTITY_DATA) instanceof NbtComponent nbtComponent
			? nbtComponent.getNbt().getString("Command").orElse("")
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
			nbt = StringNbtReader.readCompound(rawPredicate).getCompound("predicate").orElseGet(NbtCompound::new);
			if (nbt.isEmpty()) {
				throw new IllegalArgumentException("No item predicate is present!");
			}
		} catch (Throwable throwable) {
			return saveFailedPredicate(rawPredicate, throwable.getMessage());
		}
		var result = ItemPredicate.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE, MinecraftClient.getInstance().world.getRegistryManager()), nbt);
		if (result.isSuccess()) {
			var predicate = result.getOrThrow();
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

		if (ItemPredicateUtils.getPlaceholder(predicateStack) instanceof ItemStack placeholder) {
			var nbt = new NbtCompound();
			var lookup = MinecraftClient.getInstance().world.getRegistryManager();
			nbt.put("placeholder", toNbtAllowEmpty(placeholder, lookup));
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
		var count = predicate.count();
		var components = predicate.components();

		if (items.isPresent() && !stack.isIn(items.get())) {
			var msg = Text.literal("Incorrect item type. Expected: ")
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			items.get().stream()
				.flatMap(i -> Stream.of(Text.of(", "), Text.of(i.getIdAsString())))
				.skip(1)
				.forEach(msg::append);
			lines.add(msg);
		}

		if (!count.test(stack.getCount())) {
			var min = count.min();
			var max = count.max();
			var msg = Text.literal("Incorrect count. Expected: ")
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			if (min.isPresent() && max.isPresent() && min.get().equals(max.get())) {
				msg.append(Text.of(min.get().toString()));
			} else {
				if (min.isPresent()) {
					msg.append("at least " + min.get());
					if (max.isPresent()) {
						msg.append(" and ");
					}
				}
				max.ifPresent(i -> msg.append("at most " + i));
			}
			lines.add(msg);
		}

		var wrongComponents = new ArrayList<ComponentType<?>>();
		for (Map.Entry<ComponentType<?>, Optional<?>> entry : components.exact().toChanges().entrySet()) {
			ComponentType<?> type = entry.getKey();
			if (!Objects.equals(entry.getValue().orElse(null), stack.get(type))) {
				wrongComponents.add(type);
			}
		}
		if (!wrongComponents.isEmpty()) {
			var msg = Text.literal("Wrong/missing components: ")
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			wrongComponents.stream()
				.flatMap(t -> Stream.of(Text.of(", "), Text.of(Util.registryValueToString(Registries.DATA_COMPONENT_TYPE, t))))
				.skip(1)
				.forEach(msg::append);
			lines.add(msg);
		}

		var wrongSubPredicates = new ArrayList<ComponentPredicate.Type<?>>();
		for (Map.Entry<ComponentPredicate.Type<?>, ComponentPredicate> entry : components.partial().entrySet()) {
			if(!entry.getValue().test(stack)) {
				wrongSubPredicates.add(entry.getKey());
			}
		}
		if (!wrongSubPredicates.isEmpty()) {
			var msg = Text.literal("Failed sub-predicates: ")
				.styled(style -> style.withColor(Formatting.RED).withItalic(false));
			wrongSubPredicates.stream()
				.flatMap(t -> Stream.of(Text.of(", "), Text.of(Util.registryValueToString(Registries.DATA_COMPONENT_PREDICATE_TYPE, t))))
				.skip(1)
				.forEach(msg::append);
			lines.add(msg);
		}

		return lines;
	}

	@Nullable
	public static ItemStack getPlaceholder(ItemStack stack) {
		return isPredicate(stack) && stack.get(DataComponentTypes.CONTAINER) instanceof ContainerComponent containerComponent
			? containerComponent.copyFirstStack()
			: null;
	}

	public static void setPlaceholder(ItemStack predicateStack, ItemStack placeholder) {
		if (placeholder == null || placeholder.isEmpty()) {
			predicateStack.remove(DataComponentTypes.CONTAINER);
		} else {
			predicateStack.set(
				DataComponentTypes.CONTAINER,
				ContainerComponent.fromStacks(List.of(placeholder))
			);
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

	private static NbtElement toNbtAllowEmpty(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
		return stack.isEmpty() ? new NbtCompound() : stack.toNbt(registries, new NbtCompound());
	}
}
