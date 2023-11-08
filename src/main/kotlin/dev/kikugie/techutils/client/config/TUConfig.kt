package dev.kikugie.techutils.client.config

import dev.kikugie.techutils.client.TechUtilsClient
import dev.kikugie.techutils.client.config.annotation.Group
import dev.kikugie.techutils.client.config.annotation.Group.Mode
import dev.kikugie.techutils.client.config.annotation.Settings
import dev.kikugie.techutils.client.config.option.TUOption
import dev.kikugie.techutils.client.feature.browser.BrowserConfig
import dev.kikugie.techutils.client.feature.LitematicaMiscConfig
import dev.kikugie.techutils.client.feature.MiscConfig
import fi.dy.masa.malilib.config.IConfigBase
import fi.dy.masa.malilib.hotkeys.IHotkey
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

object TUConfig {
    private val classes: List<KClass<*>> = listOf(
        BrowserConfig::class,
        LitematicaMiscConfig::class,
        MiscConfig::class,
    )
    private val categoryMap = ConfigCategory.categories.associateBy(ConfigCategory::name)
    val options
        get() = categoryMap.flatMap { it.value.options }
    val hotkeys
        get() = options.mapNotNull { it.config as? IHotkey }

    init {
        classes.forEach { processClass(it) }
    }

    @Throws(IllegalArgumentException::class)
    private fun processClass(clazz: KClass<*>) {
        val group = clazz.findAnnotation<Group>()
            ?: throw IllegalArgumentException("Config class ${clazz.simpleName} is missing @Group annotation")
        val groupSettings = group.settings
        val category = categoryMap[group.category]
            ?: throw IllegalArgumentException("Config class ${clazz.simpleName} has invalid category ${group.category}.\nCategories are registered in ConfigCategory.kt")

        for (field in clazz.declaredMemberProperties) {
            val value: Any?
            try {
                value = field.getter.call(clazz.objectInstance)
            } catch (e: Exception) {
                TechUtilsClient.LOGGER.error("Failed to get value of field ${clazz.simpleName}.${field.name}")
                continue
            }
            if (value !is IConfigBase) continue

            val optionAnnotation = field.findAnnotation<Settings>()
            val optionSettings = when (group.mode) {
                Mode.NONE -> optionAnnotation ?: groupSettings
                Mode.MERGE -> if (optionAnnotation != null) Settings.merge(
                    optionAnnotation,
                    groupSettings
                ) else groupSettings

                Mode.OVERRIDE -> optionAnnotation ?: groupSettings
            }
            val option = TUOption(value, optionSettings)
            category.options.add(option)
        }
    }

    fun bootstrap() {
    }
}