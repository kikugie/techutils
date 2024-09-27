package dev.kikugie.techutils.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.kikugie.techutils.Reference;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;

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
			return this.OPTIONS;
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

}
