package me.kikugie.techutils.event;

import fi.dy.masa.malilib.hotkeys.*;
import me.kikugie.techutils.Reference;
import me.kikugie.techutils.config.Configs;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler {
    private static final InputHandler INSTANCE = new InputHandler();

    private InputHandler() {
        super();
    }

    public static InputHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (IKeybind keybind : Configs.LITEMATIC_CONFIGS.getKeybinds()) {
            manager.addKeybindToMap(keybind);
        }
        for (IKeybind keybind : Configs.WORLDEDIT_CONFIGS.getKeybinds()) {
            manager.addKeybindToMap(keybind);
        }
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(Reference.MOD_NAME, "techutils.hotkeys.category", Configs.LITEMATIC_CONFIGS.getHotkeys());
        manager.addHotkeysForCategory(Reference.MOD_NAME, "techutils.hotkeys.category", Configs.WORLDEDIT_CONFIGS.getHotkeys());
    }
}
