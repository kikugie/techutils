package me.kikugie.techutils;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import me.kikugie.techutils.config.Configs;
import me.kikugie.techutils.event.InputHandler;
import me.kikugie.techutils.event.KeyCallbacks;

public class InitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, new Configs());
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        KeyCallbacks.init();
    }
}
