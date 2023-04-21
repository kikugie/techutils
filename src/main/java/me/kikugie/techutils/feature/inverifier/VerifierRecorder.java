package me.kikugie.techutils.feature.inverifier;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

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
            hashes[i] = contents.get(i).hashCode();
        }
        BlockState state = Objects.requireNonNull(MinecraftClient.getInstance().world).getBlockState(cachedPos);
        activeEntry = new Entry(cachedPos, state, hashes);
    }

    @Nullable
    public static Entry getActive() {
        return activeEntry;
    }

    public static void close() {
        activeEntry = null;
        expectedSyncId = null;
    }

    public record Entry(BlockPos pos, BlockState state, int[] hashes) {
    }
}
