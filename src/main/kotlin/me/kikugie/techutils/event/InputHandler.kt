package me.kikugie.techutils.event

import fi.dy.masa.malilib.hotkeys.IKeybindManager
import fi.dy.masa.malilib.hotkeys.IKeybindProvider
import fi.dy.masa.malilib.hotkeys.IKeyboardInputHandler
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler
import me.kikugie.techutils.Reference
import me.kikugie.techutils.config.Configs

class InputHandler private constructor() : IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler {
    override fun addKeysToMap(manager: IKeybindManager) {
        Configs.LITEMATIC_CONFIGS.keybinds.forEach { manager.addKeybindToMap(it) }
        Configs.WORLDEDIT_CONFIGS.keybinds.forEach { manager.addKeybindToMap(it) }
        Configs.MISC_CONFIGS.keybinds.forEach { manager.addKeybindToMap(it) }
    }

    override fun addHotkeys(manager: IKeybindManager) {
        manager.addHotkeysForCategory(
            Reference.MOD_NAME,
            "techutils.hotkeys.category",
            Configs.LITEMATIC_CONFIGS.hotkeys
        )
        manager.addHotkeysForCategory(
            Reference.MOD_NAME,
            "techutils.hotkeys.category",
            Configs.WORLDEDIT_CONFIGS.hotkeys
        )
    }

    companion object {
        val INSTANCE = InputHandler()
    }
}