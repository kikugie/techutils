package dev.kikugie.techutils.feature.containerscan.verifier;

import com.chocohead.mm.api.ClassTinkerers;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier.MismatchType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SchematicVerifierExtension {
	MismatchType WRONG_INVENTORIES = ClassTinkerers.getEnum(MismatchType.class, "WRONG_INVENTORIES");
	Map<NbtElement, List<Text>> STACK_INFO_TOOLTIPS = new HashMap<>();

	List<SchematicVerifier.BlockMismatch> getSelectedInventoryMismatches$techutils();

	int getWrongInventoriesCount$techutils();
}
