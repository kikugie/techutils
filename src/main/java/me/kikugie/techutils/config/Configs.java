package me.kikugie.techutils.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import me.kikugie.techutils.Reference;
import me.kikugie.techutils.render.litematica.LitematicRenderManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Configs implements IConfigHandler {
    private static final String CONFIG_FILE_NAME = Reference.MOD_ID + ".json";
    public static LitematicConfigs LITEMATIC_CONFIGS = new LitematicConfigs();
    public static WorldEditConfigs WORLDEDIT_CONFIGS = new WorldEditConfigs();
    public static MiscConfigs MISC_CONFIGS = new MiscConfigs();

    public static void loadFromFile() {
        File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME);

        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            JsonElement element = JsonUtils.parseJsonFile(configFile);
            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();

                ConfigUtils.readConfigBase(root, "litematica", LITEMATIC_CONFIGS.get());
                ConfigUtils.readConfigBase(root, "worldedit", WORLDEDIT_CONFIGS.get());
                ConfigUtils.readConfigBase(root, "misc", MISC_CONFIGS.get());
            }
        }
    }

    public static void saveToFile() {
        File dir = FileUtils.getConfigDirectory();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            JsonObject root = new JsonObject();

            ConfigUtils.writeConfigBase(root, "litematica", LITEMATIC_CONFIGS.get());
            ConfigUtils.writeConfigBase(root, "worldedit", WORLDEDIT_CONFIGS.get());
            ConfigUtils.writeConfigBase(root, "misc", MISC_CONFIGS.get());

            JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveToFile();
    }

    public static class BaseConfigs {
        public final ImmutableList<IConfigBase> OPTIONS;

        public BaseConfigs(ImmutableList<IConfigBase> options) {
            this.OPTIONS = options;
        }

        public ImmutableList<IConfigBase> get() {
            return OPTIONS;
        }

        public ImmutableList<IHotkey> getHotkeys() {
            List<IHotkey> list = new ArrayList<>();
            for (IConfigBase configValue : this.OPTIONS) {
                if (configValue instanceof IHotkey) {
                    list.add(((IHotkey) configValue));
                }
            }
            return ImmutableList.copyOf(list);
        }

        public ImmutableList<IKeybind> getKeybinds() {
            List<IKeybind> list = new ArrayList<>();
            for (IConfigBase configValue : this.OPTIONS) {
                if (configValue instanceof IHotkey) {
                    list.add(((IHotkey) configValue).getKeybind());
                }
            }
            return ImmutableList.copyOf(list);
        }
    }

    public static class LitematicConfigs extends BaseConfigs {
        public static final ConfigBoolean RENDER_PREVIEW = new ConfigBoolean("renderPreview", true, "Show 3D render of selected litematic in Load Schematics menu\n(Works only for .litematic files)");
        public static final ConfigBoolean OVERRIDE_PREVIEW = new ConfigBoolean("overridePreview", false, "Show 3D render even if litematic has its own preview");
        public static final ConfigOptionList RENDER_ROTATION_MODE = new ConfigOptionList("rotationMode", LitematicRenderManager.RotationMode.DRAG,
                """
                        Configure model rotation mode:
                        - Mouse position: rotation follows horizontal mouse position on the screen;
                        - Free spin: rotate model at constant speed;
                        - Drag: left-click and drag mouse in the viewport viewport;
                        - Scroll: scroll mouse wheel in the viewport.""");
        public static final ConfigDouble ROTATION_FACTOR = new ConfigDouble("rotationFactor", 1, 0.1, 10, "Set model rotation sensitivity");
        public static final ConfigInteger RENDER_SLANT = new ConfigInteger("renderSlant", 30, 0, 60, "Set model vertical slant");
        public static final ConfigHotkey ROTATE_PLACEMENT = new ConfigHotkey("rotatePlacement", "R", "Rotate selected placement clockwise");
        public static final ConfigHotkey MIRROR_PLACEMENT = new ConfigHotkey("mirrorPlacement", "Y", "Cycle through selected placement's mirroring options");
        public static final ConfigBooleanHotkeyed INVENTORY_SCREEN_OVERLAY = new ConfigBooleanHotkeyed("inventoryScreenOverlay", true, "I, O", """
                Show layout of the container according to the litematic placement.
                Item colors match your placement block colors. By default its:
                - Light blue: missing item;
                - Orange: mismatched amount or nbt data;
                - Magenta: extra item that shouldn't be present;
                - Red: wrong item type.""");
        public static final ConfigHotkey VALIDATE_NBT = new ConfigHotkey("validateNbt", "", "");
        public static final ConfigHotkey CLEAR_OVERLAY = new ConfigHotkey("clearOverlay", "", "");
        public static final ConfigBoolean VALIDATE_NBT_ONLY_SAME = new ConfigBoolean("validateNbtOnlySameBlock", true, "Nbt validator will process a block if its placed as in schematic");
        public static final ConfigDouble PACKET_RATE = new ConfigDouble("packetRate", 16, 0.01, 256, false, "Number of packets sent per tick.\nWhen less than 1 approximates packet rate.\nGenerally these values shouldn't be needed unless server has strict packet restrictions (looking at you, Paper)");
        public static final ConfigInteger PACKET_TIMEOUT = new ConfigInteger("packetTimeout", 500, 1, 5000, false, "Time in milliseconds before attempting to sending packet again");
        public static final ConfigInteger QUERY_TIMEOUT = new ConfigInteger("queryTimeout", 15, 1, 600, false, "Time in seconds before query is considered failed");

        public LitematicConfigs() {
            super(ImmutableList.of(
                    RENDER_PREVIEW,
                    OVERRIDE_PREVIEW,
                    RENDER_ROTATION_MODE,
                    ROTATION_FACTOR,
                    RENDER_SLANT,
                    ROTATE_PLACEMENT,
                    MIRROR_PLACEMENT,
                    INVENTORY_SCREEN_OVERLAY

//                    VALIDATE_NBT,
//                    CLEAR_OVERLAY,
//                    VALIDATE_NBT_ONLY_SAME,
//                    PACKET_RATE,
//                    PACKET_TIMEOUT,
//                    QUERY_TIMEOUT
            ));
        }
    }

    public static class WorldEditConfigs extends BaseConfigs {
        public static final ConfigBooleanHotkeyed AUTO_WE_SYNC = new ConfigBooleanHotkeyed("autoWeSync", true, "", "Synchronise WorldEdit region to active Litematica selection");
        public static final ConfigInteger AUTO_WE_SYNC_TICKS = new ConfigInteger("autoWeSyncTicks", 10, 1, 1000, false, "Ticks to wait before synchronising WorldEdit selection\n" +
                "(Increase in case of poor connection or if you get kicked because of spam)");
        public static final ConfigBoolean AUTO_DISABLE_UPDATES = new ConfigBoolean("autoDisableUpdates", true, "Automatically disable WorldEdit neighbour updates on server join\n" +
                "(Has the same effect as running //perf neighbors off)");

        public WorldEditConfigs() {
            super(ImmutableList.of(
                    AUTO_WE_SYNC,
                    AUTO_WE_SYNC_TICKS,
                    AUTO_DISABLE_UPDATES
            ));
        }
    }

    public static class MiscConfigs extends BaseConfigs {
        public static final ConfigHotkey OPEN_CONFIG = new ConfigHotkey("openConfig", "U, C", "Opens this screen if none other is open");
        public static final ConfigBooleanHotkeyed COMPACT_SCOREBOARD = new ConfigBooleanHotkeyed("compactScoreboard", false, "F6", "Show scoreboard values in compact notation.\n" +
                "For example: 123456 -> 123.4K");

        public MiscConfigs() {
            super(ImmutableList.of(
                    OPEN_CONFIG,
                    COMPACT_SCOREBOARD
            ));
        }
    }
}
