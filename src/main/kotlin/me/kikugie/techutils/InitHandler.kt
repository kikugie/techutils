package me.kikugie.techutils

import fi.dy.masa.malilib.config.ConfigManager
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.interfaces.IInitializationHandler
import me.kikugie.techutils.config.Configs
import me.kikugie.techutils.event.InputHandler
import me.kikugie.techutils.event.KeyCallbacks

class InitHandler : IInitializationHandler {
    override fun registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, Configs())
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.INSTANCE)
        KeyCallbacks.init()
    }
}