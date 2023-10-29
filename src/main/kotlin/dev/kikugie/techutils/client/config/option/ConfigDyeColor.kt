package dev.kikugie.techutils.client.config.option

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import dev.kikugie.techutils.client.TechUtilsClient
import fi.dy.masa.malilib.config.ConfigType
import fi.dy.masa.malilib.config.IConfigValue
import fi.dy.masa.malilib.config.options.ConfigBase
import net.minecraft.util.DyeColor

class ConfigDyeColor(
    name: String,
    private var defaultColor: DyeColor?,
    private val allowNull: Boolean = false,
    comment: String
) : ConfigBase<ConfigDyeColor>(ConfigType.OPTION_LIST, name, comment), IConfigValue {
    var color = defaultColor
    private var previousColor = color

    /**
     * Set the value of this config option from a JSON element (is possible)
     * @param element
     */
    override fun setValueFromJsonElement(element: JsonElement) {
        try {
            if (!element.isJsonPrimitive)
                throw IllegalArgumentException("The value for the config option '$name' is not a string")
            color = parseColor(element.asString)
            previousColor = color
        } catch (e: Exception) {
            TechUtilsClient.LOGGER.warn(
                "Failed to set config value for '{}' from the JSON element '{}'",
                name,
                element,
                e
            )
        }
    }

    /**
     * Return the value of this config option as a JSON element, for saving into a config file.
     * @return
     */
    override fun getAsJsonElement(): JsonElement {
        return JsonPrimitive(color?.name ?: NO_COLOR)
    }

    /**
     * Returns true if the value has been changed from the default value
     * @return
     */
    override fun isModified(): Boolean {
        return color != defaultColor
    }

    /**
     * Checks whether or not the given value would be modified from the default value.
     * @param newValue
     * @return
     */
    @Throws(IllegalArgumentException::class)
    override fun isModified(newValue: String): Boolean {
        return parseColor(newValue) != defaultColor
    }

    /**
     * Resets the value back to the default value
     */
    override fun resetToDefault() {
        color = defaultColor
    }

    /**
     * Returns the String representation of the value of this config. Used in the config GUI to
     * fill in the text field contents.
     * @return the String representation of the current value
     */
    override fun getStringValue(): String {
        return color?.name ?: NO_COLOR
    }

    override fun getDefaultStringValue(): String {
        return defaultColor?.name ?: NO_COLOR
    }

    /**
     * Parses the value of this config from a String. Used for example to get the new value from
     * the config GUI textfield.
     * @param value
     */
    @Throws(IllegalArgumentException::class)
    override fun setValueFromString(value: String?) {
        color = parseColor(value ?: NO_COLOR)
    }

    @Throws(IllegalArgumentException::class)
    private fun parseColor(color: String): DyeColor? {
        val value = DyeColor.byName(color, null)
        if (value == null && !allowNull)
            throw IllegalArgumentException("The value for the config option '$name' is not a valid dye color")
        return value
    }

    companion object {
        const val NO_COLOR = "NONE"
    }
}