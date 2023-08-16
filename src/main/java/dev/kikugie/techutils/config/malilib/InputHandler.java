package dev.kikugie.techutils.config.malilib;

import dev.kikugie.techutils.Reference;
import dev.kikugie.techutils.config.Configs;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.IKeyboardInputHandler;
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler;

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
