package dev.kikugie.techutils.feature.containerscan.verifier;

import com.chocohead.mm.api.ClassTinkerers;
import com.mojang.serialization.Codec;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TranslatableTextContent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SchematicVerifierExtension {
	String ERROR_LINES_ID = "techutils:error_lines";
	Codec<List<Text>> ERROR_LINES_CODEC = TextCodecs.CODEC.listOf();
	MismatchType WRONG_INVENTORIES = ClassTinkerers.getEnum(MismatchType.class, "WRONG_INVENTORIES");

	/**
	 * One must imagine Sisyphus happy
	 */
	static @NotNull ItemStack addErrorLines(ItemStack stack, List<Text> lines) {
		var data = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (data == null || !data.getNbt().contains(ERROR_LINES_ID)) {
			return stack;
		}

		stack = stack.copy();
		var nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
		var errorLines = ERROR_LINES_CODEC.parse(NbtOps.INSTANCE, nbt.getList(ERROR_LINES_ID, NbtElement.COMPOUND_TYPE)).getOrThrow();
		nbt.remove(ERROR_LINES_ID);
		if (nbt.isEmpty()) {
			stack.remove(DataComponentTypes.CUSTOM_DATA);
			for (Text line : lines) {
				if (line.getContent() instanceof TranslatableTextContent ttc
					&& ttc.getKey().equals("item.components")
				) {
					Object[] args = ttc.getArgs();
					args[0] = ((int) args[0]) - 1;
				}
			}
		} else {
			stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
		}
		lines.addAll(errorLines);

		return stack;
	}

	List<SchematicVerifier.BlockMismatch> getSelectedInventoryMismatches$techutils();

	int getWrongInventoriesCount$techutils();
}
