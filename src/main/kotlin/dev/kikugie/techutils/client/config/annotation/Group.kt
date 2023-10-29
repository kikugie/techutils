package dev.kikugie.techutils.client.config.annotation

import me.fallenbreath.conditionalmixin.api.annotation.Restriction

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Group(
    val category: String,
    val settings: Settings = Settings(Restriction()),
    val mode: Mode = Mode.MERGE
) {
    companion object {
        const val LITEMATICA = "litematica"
        const val MISC = "misc"
        fun key(group: Group): String = "techutils.group.${group.category}"
    }

    enum class Mode {
        NONE,
        MERGE,
        OVERRIDE
    }
}
