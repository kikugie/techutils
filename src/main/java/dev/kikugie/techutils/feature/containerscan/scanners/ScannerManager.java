package dev.kikugie.techutils.feature.containerscan.scanners;

import fi.dy.masa.litematica.data.DataManager;
import org.jetbrains.annotations.Nullable;

public class ScannerManager {
    @Nullable
    private static Scanner scanner = null;

    public static void onKey() {
        if (scanner == null)
            run();
        else
            close();
    }

    private static void run() {
        if (scanner == null)
            scanner = new InteractionScanner(DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement());
        else
            scanner.start();
    }

    public static void tick() {
        if (scanner != null)
            scanner.tick();
    }

    private static void pause() {
        if (scanner != null)
            scanner.stop();
    }

    public static void close() {
        scanner = null;
    }

    public static void update() {
        if (scanner != null)
            scanner.update();
    }
}
