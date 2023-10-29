package dev.kikugie.techutils.client.config.compat

import dev.kikugie.techutils.Reference
import dev.kikugie.techutils.client.config.TUConfig
import dev.kikugie.techutils.client.config.TUConfigStorage
import fi.dy.masa.malilib.config.ConfigManager
import fi.dy.masa.malilib.event.InitializationHandler
import fi.dy.masa.malilib.event.InputEventHandler
import fi.dy.masa.malilib.hotkeys.IKeybindManager
import fi.dy.masa.malilib.hotkeys.IKeybindProvider
import fi.dy.masa.malilib.hotkeys.IKeyboardInputHandler
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler
import fi.dy.masa.malilib.interfaces.IInitializationHandler

object TUMalilibInitializer : IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler, IInitializationHandler {
    fun init() {
        InitializationHandler.getInstance().registerInitializationHandler(this)
    }

    /**
     * Called when the keybind map is refreshed/recreated.
     * Classes implementing this interface should add all of their keybinds
     * using the [IKeybindManager.addKeybindToMap] method when this method is called.
     * Assume any previously added keybinds have been cleared just before this method is called.
     * @param manager
     */
    override fun addKeysToMap(manager: IKeybindManager) {
        TUConfig.hotkeys.forEach {
            manager.addKeybindToMap(it.keybind)
        }
    }

    /**
     * Called when the event handler is registered.
     * Any mod that wants all their keybinds to appear in the master/combined list of all
     * keybinds, should add them here using the [IKeybindManager.addHotkeysForCategory] method).
     * @param manager
     */
    override fun addHotkeys(manager: IKeybindManager) {
        manager.addHotkeysForCategory(Reference.MOD_NAME, "techutils.keyCategory", TUConfig.hotkeys)
    }

    /**
     * This method will be called for any registered **IInitializationHandler**
     * when the game has been initialized and the mods can register their keybinds and configs
     * to malilib without causing class loading issues.
     * <br></br><br></br>
     * So call all your (malilib-facing) mod init stuff inside this handler!
     */
    override fun registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_NAME, TUConfigStorage())
        InputEventHandler.getKeybindManager().registerKeybindProvider(this)
    }
}