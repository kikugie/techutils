package dev.kikugie.techutils.config

import dev.kikugie.malilib_extras.api.config.ConfigBuilder
import dev.kikugie.malilib_extras.api.option.OptionFactory
import dev.kikugie.techutils.Reference

internal val FACTORY = OptionFactory.compact(Reference.MOD_ID)
internal const val LITEMATICA = "litematica"
internal const val MISC = "misc"

fun ConfigBuilder.register(any: Any) = register(any::class)