package dev.kikugie.techutils.feature.containerscan;

import dev.kikugie.techutils.TechUtilsMod;
import dev.kikugie.techutils.util.ContainerUtils;
import dev.kikugie.techutils.util.LocalPlacementPos;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Used to access container data inside a placement.
 * <p>
 * Is this overcomplicated? Definitely.
 */
public final class PlacementContainerAccess {
    public static LinkedStorageEntry getEntry(BlockPos worldPos, BlockState worldState) {
        return new LinkedStorageEntry(worldPos, null, getSchematicInventory(worldPos, worldState).orElse(null));
    }

    public static LinkedStorageEntry getEntry(BlockPos worldPos, BlockState worldState, SimpleInventory worldInventory) {
        return new LinkedStorageEntry(worldPos, worldInventory, getSchematicInventory(worldPos, worldState).orElse(null));
    }

    /**
     * Gets placement container data at a position, if there's a placement there. For double chests returns their combined inventory.
     *
     * @param worldPos   position in the world
     * @param worldState block state of the given position
     * @return {@link SimpleInventory} with schematic items if its present
     * @see LocalPlacementPos
     */
    public static Optional<SimpleInventory> getSchematicInventory(BlockPos worldPos, BlockState worldState) {
        ChestType type = getChestType(worldState);
        if (type == ChestType.SINGLE)
            return getSchematicInventoryInternal(worldPos, worldState);

        // Double chest handling
        BlockPos adjacentChest = worldPos.add(ChestBlock.getFacing(worldState).getVector());
        assert MinecraftClient.getInstance().world != null;
        BlockState adjacentState = MinecraftClient.getInstance().world.getBlockState(adjacentChest);

        Optional<SimpleInventory> opt1 = getSchematicInventoryInternal(worldPos, worldState);
        Optional<SimpleInventory> opt2 = getSchematicInventoryInternal(adjacentChest, adjacentState);
        if (opt1.isEmpty() && opt2.isEmpty())
            return Optional.empty();
        SimpleInventory chest1 = opt1.orElse(new SimpleInventory(27));
        SimpleInventory chest2 = opt2.orElse(new SimpleInventory(27));

        return type == ChestType.RIGHT ? Optional.of(merge(chest1, chest2)) : Optional.of(merge(chest2, chest1));
    }

    public static Optional<SimpleInventory> getSchematicInventoryInternal(BlockPos worldPos, BlockState worldState) {
        Optional<Inventory> dummyInv = ContainerUtils.validateContainer(worldPos, worldState);
        // World block is not a container
        if (dummyInv.isEmpty())
            return Optional.empty();

        Optional<LocalPlacementPos> optionalPos = LocalPlacementPos.get(worldPos);
        // Block is not in the schematic
        if (optionalPos.isEmpty())
            return Optional.empty();

        LocalPlacementPos placementPos = optionalPos.get();
        // Schematic and world blocks don't match
        if (!placementPos.blockState().equals(worldState))
            return Optional.empty();

        return Optional.ofNullable(getItems(placementPos, (byte) dummyInv.get().size()));
    }

    private static SimpleInventory merge(Inventory first, Inventory second) {
        DoubleInventory combined = new DoubleInventory(first, second);
        SimpleInventory inventory = new SimpleInventory(combined.size());
        for (int i = 0; i < combined.size(); i++) {
            inventory.setStack(i, combined.getStack(i));
        }
        return inventory;
    }

    private static void sendError(String message) {
        TechUtilsMod.LOGGER.warn(message);
    }

    /**
     * @return {@link ChestType} of a block state. Returns {@link ChestType#SINGLE} for any other state, as all that matters is if it's a single-block storage.
     */
    private static ChestType getChestType(BlockState state) {
        if (!(state.getBlock() instanceof ChestBlock))
            return ChestType.SINGLE;
        return state.get(ChestBlock.CHEST_TYPE);
    }

    @Nullable
    private static SimpleInventory getItems(LocalPlacementPos placementPos, byte size) {
        Map<BlockPos, NbtCompound> blockEntities = placementPos.placement().getSchematic()
                .getBlockEntityMapForRegion(placementPos.region());
        // No block entity map for the region. Shouldn't be possible unless it was manually modified
        if (blockEntities == null)
            return null;

        NbtCompound nbt = blockEntities.get(placementPos.pos());
        // No such entry in the map
        if (nbt == null)
            return null;

        NbtList schematicItems = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
        // Entry is not an inventory
        if (schematicItems == null)
            return null;

        SimpleInventory inventory = new SimpleInventory(size);
        // Not technically necessary, but skips the loop
        if (schematicItems.isEmpty())
            return inventory;

        for (byte i = 0; i < schematicItems.size(); i++) {
            NbtCompound item = schematicItems.getCompound(i);
            byte slot = item.getByte("Slot");
            if (slot >= size) {
                sendError("Schematic item slot %d is outside of the allowed size %d".formatted(slot, size));
                return null;
            }
            inventory.setStack(slot, ItemStack.fromNbt(item));
        }
        return inventory;
    }
}
