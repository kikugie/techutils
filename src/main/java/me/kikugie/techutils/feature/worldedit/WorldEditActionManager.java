package me.kikugie.techutils.feature.worldedit;

import fi.dy.masa.litematica.data.DataManager;
import me.kikugie.techutils.TechUtilsMod;
import me.kikugie.techutils.config.Configs;
import me.kikugie.techutils.networking.WorldEditNetworkHandler;
import me.kikugie.techutils.utils.ResponseMuffler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


public class WorldEditActionManager {
    private static WorldEditActionManager instance;
    private final WorldEditNetworkHandler handler = WorldEditNetworkHandler.initHandler();
    private final MinecraftClient client = MinecraftClient.getInstance();
    private Box lastBox;
    private int syncTimer = -1;
    private boolean initComplete = false;

    private WorldEditActionManager() {
    }

    public static WorldEditActionManager initManager() {
        instance = new WorldEditActionManager();
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public static WorldEditActionManager getInstance() {
        return instance;
    }

    public void onWorldEditConnected() {
        ClientTickEvents.START_WORLD_TICK.register(tick -> {
            if (!client.player.hasPermissionLevel(2)) return;
            if (Configs.WorldEditConfigs.AUTO_DISABLE_UPDATES.getBooleanValue()) disableNeighborUpdates();
            if (Configs.WorldEditConfigs.AUTO_WE_SYNC.getBooleanValue()) syncSelection();
            if (!initComplete) initComplete = true;
        });
    }

    private void disableNeighborUpdates() {
        if (initComplete) return;
        ResponseMuffler.scheduleMute("Side effect \"Neighbors\".+");
        Objects.requireNonNull(client.getNetworkHandler()).sendCommand("/perf neighbors off");
        TechUtilsMod.LOGGER.debug("Turning off perf");
    }

    private void syncSelection() {
        if (syncTimer > 0) syncTimer--;
        if (!handler.storage.isCuboidMode()) return;

        var box = getValidActiveSelection();
        if (box == null) return;

        var mathBox = new Box(box.getPos1(), box.getPos2());
        if (!mathBox.equals(lastBox)) {
            lastBox = mathBox;
            syncTimer = Configs.WorldEditConfigs.AUTO_WE_SYNC_TICKS.getIntegerValue();
            return;
        }
        if (syncTimer != 0) return;

        syncTimer = -1;
        updateRegion(box);
        TechUtilsMod.LOGGER.debug("WorldEdit synced!");
        client.player.sendMessage(Text.translatable("techutils.feedback.wesync.success"), true);
    }

    public void updateRegion(fi.dy.masa.litematica.selection.Box box) {
        ResponseMuffler.scheduleMute("(\\w+ position set to \\(.+\\).)|(Position already set.)");
        client.getNetworkHandler().sendCommand(String.format("/pos1 %d,%d,%d",
                box.getPos1().getX(),
                box.getPos1().getY(),
                box.getPos1().getZ()));
        ResponseMuffler.scheduleMute("(\\w+ position set to \\(.+\\).)|(Position already set.)");
        client.getNetworkHandler().sendCommand(String.format("/pos2 %d,%d,%d",
                box.getPos2().getX(),
                box.getPos2().getY(),
                box.getPos2().getZ()));
    }

    @Nullable
    public fi.dy.masa.litematica.selection.Box getValidActiveSelection() {
        var selection = DataManager.getSelectionManager().getCurrentSelection();
        if (selection == null) {
            return null;
        }
        var box = selection.getSelectedSubRegionBox();
        if (box == null || box.getPos1() == null || box.getPos2() == null) {
            return null;
        }
        return box;
    }
}
