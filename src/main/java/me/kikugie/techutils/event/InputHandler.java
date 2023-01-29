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
        Configs.LITEMATIC_CONFIGS.getKeybinds().forEach(manager::addKeybindToMap);
        Configs.WORLDEDIT_CONFIGS.getKeybinds().forEach(manager::addKeybindToMap);
        Configs.MISC_CONFIGS.getKeybinds().forEach(manager::addKeybindToMap);
    }

    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(Reference.MOD_NAME, "techutils.hotkeys.category", Configs.LITEMATIC_CONFIGS.getHotkeys());
        manager.addHotkeysForCategory(Reference.MOD_NAME, "techutils.hotkeys.category", Configs.WORLDEDIT_CONFIGS.getHotkeys());
        manager.addHotkeysForCategory(Reference.MOD_NAME, "techutils.hotkeys.category", Configs.MISC_CONFIGS.getHotkeys());
    }
}
