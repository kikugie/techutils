package me.kikugie.techutils.feature.inverifier;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.util.SchematicUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ContainerStorage {
    private static final ItemStack[] NO_ITEMS = new ItemStack[0];
    private static final Map<SchematicPlacement, ContainerStorage> placements = new WeakHashMap<>();
    private final SchematicPlacement placement;
    private final Map<BlockPos, int[]> containerCache = new HashMap<>();

    public ContainerStorage(SchematicPlacement placement) {
        this.placement = placement;
    }

    @Nullable
    public static SimpleInventory getSchematicInventory(BlockPos pos, BlockState state) {
        ItemStack[] items = (state.getBlock() instanceof ChestBlock) ? getChestItems(pos, state) : getItemsInternal(pos);
        return items == null ? null : new SimpleInventory(items);
    }

    @Nullable
    private static ItemStack[] getChestItems(BlockPos pos, BlockState state) {
        if (state == null || !(state.getBlock() instanceof ChestBlock)) return null;
        ChestType type = state.get(ChestBlock.CHEST_TYPE);
        ItemStack[] items1 = getItemsInternal(pos);
        if (type == ChestType.SINGLE) return items1;

        BlockPos pos2 = pos.add(ChestBlock.getFacing(state).getVector());
        ItemStack[] items2 = getItemsInternal(pos2);
        if (items1 == null || items2 == null) return null;

        ItemStack[] items = new ItemStack[54];
        switch (type) {
            case LEFT -> {
                System.arraycopy(items1, 0, items, 0, items1.length);
                System.arraycopy(items2, 0, items, 27, items2.length);
            }
            case RIGHT -> {
                System.arraycopy(items2, 0, items, 0, items2.length);
                System.arraycopy(items1, 0, items, 27, items1.length);
            }
        }
        return items;
    }

    @Nullable
    private static ItemStack[] getItemsInternal(BlockPos pos) {
        PosInPlacement placement = getPlacementBlock(pos);
        if (placement == null) return null;

        NbtCompound nbt = Objects.requireNonNull(placement.placement.getSchematic()
                        .getBlockEntityMapForRegion(placement.region))
                .get(placement.pos);
        if (nbt == null) return null;
        NbtList schematicItems = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
        if (schematicItems == null) return NO_ITEMS;

        Int2ObjectArrayMap<ItemStack> items = new Int2ObjectArrayMap<>();
        int maxSlot = 0;

        for (int i = 0; i < schematicItems.size(); i++) {
            NbtCompound item = schematicItems.getCompound(i);
            int slot = item.getByte("Slot");
            maxSlot = Math.max(maxSlot, slot);
            items.put(slot, ItemStack.fromNbt(item));
        }

        ItemStack[] returnItems = new ItemStack[maxSlot + 1];
        for (int i = 0; i < returnItems.length; i++) {
            returnItems[i] = items.get(i);
        }
        return returnItems;
    }

    @Nullable
    private static ContainerStorage.PosInPlacement getPlacementBlock(BlockPos pos) {
        List<SchematicPlacementManager.PlacementPart> parts = DataManager.getSchematicPlacementManager().getAllPlacementsTouchingChunk(pos);

        for (SchematicPlacementManager.PlacementPart part : parts) {
            if (!part.getBox().containsPos(pos)) continue;

            SchematicPlacement placement = part.placement;
            String region = part.subRegionName;
            LitematicaBlockStateContainer container = placement.getSchematic().getSubRegionContainer(region);
            BlockPos schematicPos = SchematicUtils.getSchematicContainerPositionFromWorldPosition(
                    pos,
                    placement.getSchematic(),
                    region,
                    placement,
                    Objects.requireNonNull(
                            placement.getRelativeSubRegionPlacement(region),
                            "Somehow subregion is null"),
                    container);
            return new PosInPlacement(placement, region, schematicPos);
        }
        return null;
    }

    public record PosInPlacement(SchematicPlacement placement, String region, BlockPos pos) {
    }
}
