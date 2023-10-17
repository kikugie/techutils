package dev.kikugie.techutils.config

import fi.dy.masa.malilib.config.IConfigBase

interface ConfigGroup {
    fun getConfigs(): Collection<IConfigBase>
}