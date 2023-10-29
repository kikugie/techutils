package dev.kikugie.techutils.client.config.annotation

import me.fallenbreath.conditionalmixin.api.annotation.Restriction

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Settings(
    val restriction: Restriction = Restriction(require = [], conflict = []),
    val requirement: Array<String> = [],
    val debug: Boolean = false,
    val dev: Boolean = false,
) {
    companion object {
        fun merge(set1: Settings, set2: Settings): Settings {
            val res1 = set1.restriction
            val res2 = set2.restriction
            return Settings(
                Restriction(require = res1.require + res2.require, conflict = res1.conflict + res2.conflict),
                set1.requirement + set2.requirement,
                set1.debug || set2.debug,
                set1.dev || set2.dev
            )
        }
    }
}
