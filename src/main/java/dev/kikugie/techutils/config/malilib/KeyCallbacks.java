package dev.kikugie.techutils.config.malilib;

import dev.kikugie.techutils.config.ConfigGui;
import dev.kikugie.techutils.config.LitematicConfigs;
import dev.kikugie.techutils.config.MiscConfigs;
import dev.kikugie.techutils.feature.GiveFullIInv;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;

import java.util.function.Function;


public class KeyCallbacks {
	public static void init() {
		ConditionalCallback.set(MiscConfigs.GIVE_FULL_INV, action -> GiveFullIInv.onKeybind());

		ConditionalCallback.set(LitematicConfigs.ROTATE_PLACEMENT, action -> {
			SchematicPlacement placement = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
			if (placement == null)
				return false;
			placement.setRotation(placement.getRotation().rotate(BlockRotation.CLOCKWISE_90), InGameNotifier.INSTANCE);
			return true;
		});
		ConditionalCallback.set(LitematicConfigs.MIRROR_PLACEMENT, action -> {
			SchematicPlacement placement = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
			if (placement == null)
				return false;
			BlockMirror mirror = switch (placement.getMirror()) {
				case NONE -> BlockMirror.LEFT_RIGHT;
				case LEFT_RIGHT -> BlockMirror.FRONT_BACK;
				case FRONT_BACK -> BlockMirror.NONE;
			};
			placement.setMirror(mirror, InGameNotifier.INSTANCE);
			return true;
		});
		ConditionalCallback.set(LitematicConfigs.REFRESH_MATERIAL_LIST, action -> {
			var materialList = DataManager.getMaterialList();
			if (materialList == null)
				return false;
			materialList.reCreateMaterialList();
			return true;
		});
		ConditionalCallback.set(MiscConfigs.OPEN_CONFIG, action -> {
			MinecraftClient.getInstance().setScreen(new ConfigGui());
			return true;
		});
		ConditionalCallback.set(MiscConfigs.GIVE_FULL_INV, action -> GiveFullIInv.onKeybind());
	}

	private record ConditionalCallback(IKeybind keybind, Function<KeyAction, Boolean> callback)
		implements IHotkeyCallback {
		public static void set(IHotkey option, Function<KeyAction, Boolean> callback) {
			option.getKeybind().setCallback(new ConditionalCallback(option.getKeybind(), callback));
		}

		@Override
		public boolean onKeyAction(KeyAction action, IKeybind key) {
			if (key != this.keybind)
				return false;
			return this.callback.apply(action);
		}
	}
}
