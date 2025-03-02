package dev.kikugie.techutils.feature.containerscan.verifier;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public interface BlockMismatchExtension<InventoryBE extends BlockEntity & Inventory> {
	void setInventories$techutils(Pair<InventoryBE, InventoryBE> inventories);

	@Nullable
	Pair<InventoryBE, InventoryBE> getInventories$techutils();
}
