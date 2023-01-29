package me.kikugie.techutils.config

import com.google.common.collect.ImmutableList
import com.google.gson.JsonObject
import fi.dy.masa.malilib.config.ConfigUtils
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.config.IConfigHandler
import fi.dy.masa.malilib.config.options.*
import fi.dy.masa.malilib.hotkeys.IHotkey
import fi.dy.masa.malilib.hotkeys.IKeybind
import fi.dy.masa.malilib.util.FileUtils
import fi.dy.masa.malilib.util.JsonUtils
import me.kikugie.techutils.Reference
import me.kikugie.techutils.render.litematica.LitematicRenderManager.RotationMode
import java.io.File

class Configs : IConfigHandler {
    override fun load() {
        loadFromFile()
    }

    override fun save() {
        saveToFile()
    }

    abstract class BaseConfigs {
        abstract val options: ImmutableList<IConfigBase>
        fun get(): ImmutableList<IConfigBase> {
            return options
        }

        val hotkeys: ImmutableList<IHotkey>
            get() {
                val list: MutableList<IHotkey> = ArrayList()
                for (configValue in options) {
                    if (configValue is IHotkey) {
                        list.add(configValue)
                    }
                }
                return ImmutableList.copyOf(list)
            }
        val keybinds: ImmutableList<IKeybind>
            get() {
                val list: MutableList<IKeybind> = ArrayList()
                for (configValue in options) {
                    if (configValue is IHotkey) {
                        list.add(configValue.keybind)
                    }
                }
                return ImmutableList.copyOf(list)
            }
    }

    object LitematicConfigs : BaseConfigs() {
        @JvmStatic
        val RENDER_PREVIEW = ConfigBoolean("renderPreview", true, "Show 3D render of schematic")

        @JvmStatic
        val OVERRIDE_PREVIEW =
            ConfigBoolean("overridePreview", false, "Show render even if schematic has its own preview")

        @JvmStatic
        val RENDER_ROTATION_MODE = ConfigOptionList(
            "rotationMode",
            RotationMode.DRAG,
            "- Free Spin: rotation follows mouse position;\n- Drag: drag mouse in viewport;\nScroll: scroll mouse view in viewport"
        )

        @JvmStatic
        val RENDER_SLANT = ConfigInteger("renderSlant", 30, 0, 60, "Slant of the render")

        @JvmStatic
        val ROTATE_PLACEMENT = ConfigHotkey("rotatePlacement", "", "Rotates selected placement clockwise")

        @JvmStatic
        val MIRROR_PLACEMENT =
            ConfigHotkey("mirrorPlacement", "", "Cycles through selected placement's mirroring options")

        override val options: ImmutableList<IConfigBase> = ImmutableList.of(
            RENDER_PREVIEW,
            OVERRIDE_PREVIEW,
            RENDER_ROTATION_MODE,
            RENDER_SLANT,
            ROTATE_PLACEMENT,
            MIRROR_PLACEMENT
        )
    }

    object WorldEditConfigs : BaseConfigs() {
        @JvmStatic
        val AUTO_WE_SYNC = ConfigBooleanHotkeyed(
            "autoWeSync",
            false,
            "",
            "Synchronises WorldEdit selection n ticks after configured value"
        )

        @JvmStatic
        val AUTO_WE_SYNC_TICKS = ConfigInteger(
            "autoWeSyncTicks",
            10,
            1,
            1000,
            false,
            "Ticks to wait before synchronising WorldEdit selection"
        )

        @JvmStatic
        val AUTO_DISABLE_UPDATES =
            ConfigBoolean(
                "autoDisableUpdates",
                true,
                "Automatically disables WorldEdit neighbour updates on log in"
            )

        override val options: ImmutableList<IConfigBase> = ImmutableList.of(
            AUTO_WE_SYNC,
            AUTO_WE_SYNC_TICKS,
            AUTO_DISABLE_UPDATES
        )
    }

    object MiscConfigs : BaseConfigs() {
        @JvmStatic
        val MOJANK = ConfigBoolean("mojank", true, "No description provided")

        @JvmStatic
        val COMPACT_SCOREBOARD = ConfigBooleanHotkeyed(
            "compactScoreboard",
            false,
            "F6",
            "Show scoreboard values in compact notation.\nFor example: 123456 -> 123.4K"
        )
        override val options: ImmutableList<IConfigBase> = ImmutableList.of(
            COMPACT_SCOREBOARD
        )
    }

    companion object {
        private const val CONFIG_FILE_NAME = Reference.MOD_ID + ".json"
        var LITEMATIC_CONFIGS = LitematicConfigs
        var WORLDEDIT_CONFIGS = WorldEditConfigs
        var MISC_CONFIGS = MiscConfigs
        fun loadFromFile() {
            val configFile = File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME)
            if (!(configFile.exists() && configFile.isFile && configFile.canRead())) return

            val element = JsonUtils.parseJsonFile(configFile)
            if (element != null && element.isJsonObject) {
                val root = element.asJsonObject
                ConfigUtils.readConfigBase(root, "litematica", LITEMATIC_CONFIGS.get())
                ConfigUtils.readConfigBase(root, "worldedit", WORLDEDIT_CONFIGS.get())
                ConfigUtils.readConfigBase(root, "misc", MISC_CONFIGS.get())
            }
        }

        fun saveToFile() {
            val dir = FileUtils.getConfigDirectory()
            if (!(dir.exists() && dir.isDirectory || dir.mkdirs())) return

            val root = JsonObject()
            ConfigUtils.writeConfigBase(root, "litematica", LITEMATIC_CONFIGS.get())
            ConfigUtils.writeConfigBase(root, "worldedit", WORLDEDIT_CONFIGS.get())
            ConfigUtils.writeConfigBase(root, "misc", MISC_CONFIGS.get())
            JsonUtils.writeJsonToFile(root, File(dir, CONFIG_FILE_NAME))
        }
    }
}