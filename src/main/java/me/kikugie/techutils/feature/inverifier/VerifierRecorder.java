package me.kikugie.techutils.feature.inverifier;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VerifierRecorder {
    private static Entry activeEntry = null;
    private static BlockPos cachedPos = null;
    @Nullable
    private static Integer expectedSyncId = null;

    public static void onContainerClick(BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        BlockEntity blockEntity = MinecraftClient.getInstance().world != null ? MinecraftClient.getInstance().world.getBlockEntity(pos) : null;
        if (blockEntity instanceof LootableContainerBlockEntity) {
            activeEntry = null;
            cachedPos = pos;
        }
    }

    public static void onScreenPacket(int syncId) {
        expectedSyncId = syncId;
    }

    public static void onInventoryPacket(int syncId, List<ItemStack> contents) {
        if (expectedSyncId == null || syncId != expectedSyncId || cachedPos == null) {
            close();
            return;
        }
        int[] hashes = new int[contents.size()];
        for (int i = 0; i < contents.size(); i++) {
            ItemStack stack = contents.get(i);
            hashes[i] = stack != null ? contents.hashCode() : ItemStack.EMPTY.hashCode();
        }
        if (activeEntry != null) {
            activeEntry.updateHashes(hashes);
        }

        BlockState state = Objects.requireNonNull(MinecraftClient.getInstance().world).getBlockState(cachedPos);
        SimpleInventory schematicInventory = ContainerStorage.getSchematicInventory(cachedPos, state);
        activeEntry = new Entry(cachedPos, state, hashes, schematicInventory);
    }

    @Nullable
    public static Entry getActive() {
        return activeEntry;
    }

    public static void close() {
        activeEntry = null;
        expectedSyncId = null;
    }

    public static final class Entry {
        public final BlockPos pos;
        public final BlockState state;
        @Nullable
        public final SimpleInventory schematicInv;
        public int[] hashes;
        private boolean matching = false;

        public Entry(BlockPos pos, BlockState state, int[] hashes, @Nullable SimpleInventory schematicInv) {
            this.pos = pos;
            this.state = state;
            this.schematicInv = schematicInv;
            updateHashes(hashes);
        }

        public void updateHashes(int[] hashes) {
            this.hashes = hashes;
            if (schematicInv == null)
                return;

            int[] schemHashes = new int[schematicInv.size()];
            for (int i = 0; i < schematicInv.size(); i++) {
                ItemStack stack = schematicInv.getStack(i);
                schemHashes[i] = stack != null ? stack.hashCode() : ItemStack.EMPTY.hashCode();
            }
            this.matching = Arrays.equals(hashes, schemHashes);
        }

        public boolean matching() {
            return matching;
        }
    }
}
