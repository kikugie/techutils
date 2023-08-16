package dev.kikugie.techutils.feature.containerscan;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents contents of an existing container and one according to a placement at the same position.
 */
public class LinkedStorageEntry {
    private static final Supplier<Color4f> MISSING_COLOR = Configs.Colors.SCHEMATIC_OVERLAY_COLOR_MISSING::getColor;
    private static final Supplier<Color4f> WRONG_COLOR = Configs.Colors.SCHEMATIC_OVERLAY_COLOR_WRONG_BLOCK::getColor;
    private static final Supplier<Color4f> MISMATCHED_COLOR = Configs.Colors.SCHEMATIC_OVERLAY_COLOR_WRONG_STATE::getColor;
    private static final Supplier<Color4f> EXTRA_COLOR = Configs.Colors.SCHEMATIC_OVERLAY_COLOR_EXTRA::getColor;
    public final BlockPos pos;
    @Nullable
    private Inventory worldInventory;
    @Nullable
    private Inventory placementInventory;

    public LinkedStorageEntry(BlockPos pos, @Nullable Inventory worldInventory, @Nullable Inventory placementInventory) {
        this.pos = pos;
        this.worldInventory = worldInventory;
        this.placementInventory = placementInventory;
    }

    public Optional<Inventory> getWorldInventory() {
        return Optional.ofNullable(this.worldInventory);
    }

    public void setWorldInventory(@Nullable Inventory inventory) {
        this.worldInventory = inventory;
    }

    public Optional<Inventory> getPlacementInventory() {
        return Optional.ofNullable(this.placementInventory);
    }

    public void setPlacementInventory(@Nullable Inventory inventory) {
        this.placementInventory = inventory;
    }

    public Optional<Color4f> validate() {
        if (this.worldInventory == null || this.placementInventory == null)
            return Optional.empty();

        int type = 0;
        for (int i = 0; i < this.worldInventory.size(); i++) {
            ItemStack world = this.worldInventory.getStack(i);
            ItemStack schem = this.placementInventory.getStack(i);

            if (world.isEmpty() && !schem.isEmpty())
                type = Math.max(type, 1);
            else if (!world.isEmpty() && schem.isEmpty())
                type = Math.max(type, 2);
            else if (!world.getItem().equals(schem.getItem()))
                type = Math.max(type, 4);
            else if (world.getCount() != schem.getCount())
                type = Math.max(type, 3);
        }

        return switch (type) {
            case 1 -> Optional.of(MISSING_COLOR.get());
            case 2 -> Optional.of(EXTRA_COLOR.get());
            case 3 -> Optional.of(MISMATCHED_COLOR.get());
            case 4 -> Optional.of(WRONG_COLOR.get());
            default -> Optional.empty();
        };
    }
}
