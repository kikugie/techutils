package me.kikugie.techutils.networking;

import net.minecraft.util.math.BlockPos;

public class WorldEditStorage {
    private final BlockPos[] region = new BlockPos[2];
    private boolean isCuboidMode;

    public void setPos(int n, BlockPos pos) {
        region[n] = pos;
    }

    public BlockPos getPos(int n) {
        return region[n];
    }

    public BlockPos[] getRegion() {
        return region;
    }

    public boolean isComplete() {
        return region[0] != null && region[1] != null;
    }

    public boolean isCuboidMode() {
        return isCuboidMode;
    }

    public void parseMode(String mode) {
        isCuboidMode = mode.equals("cuboid");
    }
}
