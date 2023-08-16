package dev.kikugie.techutils.util;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

import java.util.Optional;

public class ContainerUtils {
    public static Optional<Inventory> validateContainer(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BlockEntityProvider provider) {
            BlockEntity dummy = provider.createBlockEntity(pos, state);
            if (dummy instanceof LockableContainerBlockEntity inventory)
                return Optional.of(inventory);
        }
        return Optional.empty();
    }

    public static boolean isChestAccessible(WorldAccess world, BlockPos pos, BlockState state) {
        assert state.getBlock() instanceof ChestBlock;
        if (ChestBlock.isChestBlocked(world, pos))
            return false;

        ChestType type = state.get(ChestBlock.CHEST_TYPE);
        if (type == ChestType.SINGLE)
            return true;

        BlockPos adjacent = pos.add(ChestBlock.getFacing(state).getVector());
        return world.getBlockState(adjacent).getBlock() != state.getBlock()
                || !ChestBlock.isChestBlocked(world, adjacent);
    }

    public static boolean isShulkerBoxAccessible(WorldAccess world, BlockPos pos, BlockState state) {
        assert state.getBlock() instanceof ShulkerBoxBlock;
        ShulkerBoxBlockEntity box = (ShulkerBoxBlockEntity) world.getBlockEntity(pos);
        if (box == null || box.getAnimationStage() != ShulkerBoxBlockEntity.AnimationStage.CLOSED)
            return false;

        return world.isSpaceEmpty(ShulkerEntity
                .calculateBoundingBox(state.get(ShulkerBoxBlock.FACING), 0.0F, 0.5F)
                .offset(pos).contract(1.0E-6D));
    }
}
