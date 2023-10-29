package dev.kikugie.techutils.client.config.option

import fi.dy.masa.malilib.config.options.*
import fi.dy.masa.malilib.hotkeys.KeybindSettings

object Options {
    private const val DESCRIPTION_FORMATTER = "techutils.config.%s"
    fun create(name: String, default: Boolean = true) = ConfigBoolean(name, default, DESCRIPTION_FORMATTER.format(name))
    fun create(
        name: String,
        default: Int = 0,
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE,
        slider: Boolean = false
    ) = ConfigInteger(name, default, min, max, slider, DESCRIPTION_FORMATTER.format(name))

    fun create(
        name: String,
        default: Double = 0.0,
        min: Double = Double.MIN_VALUE,
        max: Double = Double.MAX_VALUE,
        slider: Boolean = false
    ) = ConfigDouble(name, default, min, max, slider, DESCRIPTION_FORMATTER.format(name))

    fun create(name: String, keybind: String, settings: KeybindSettings = KeybindSettings.DEFAULT, callback: (() ->Boolean)? = null): ConfigHotkey {
        val out = ConfigHotkey(name, keybind, settings, DESCRIPTION_FORMATTER.format(name))
        if (callback != null) out.keybind.setCallback{ _, _ -> callback() }
        return out
    }

    fun create(name: String, default: Boolean, keybind: String, settings: KeybindSettings = KeybindSettings.DEFAULT, callback: (() ->Boolean)? = null): ConfigBooleanHotkeyed {
        val out = ConfigBooleanHotkeyed(name, default, keybind, settings, DESCRIPTION_FORMATTER.format(name), name)
        if (callback != null) out.keybind.setCallback { _, _ -> callback() }
        return out
    }
}