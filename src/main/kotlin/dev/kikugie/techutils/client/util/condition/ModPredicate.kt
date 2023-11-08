package dev.kikugie.techutils.client.util.condition

import dev.kikugie.techutils.client.util.fitsAnyPredicate
import dev.kikugie.techutils.client.util.isLoaded
import me.fallenbreath.conditionalmixin.api.annotation.Condition


class ModPredicate private constructor(val mod: String, val versionPredicates: List<String>) {
    val isSatisfied: Boolean = isLoaded(mod) && fitsAnyPredicate(mod, versionPredicates)
    val versionPredicatesString: String
        get() = versionPredicates.joinToString(" || ")

    override fun toString(): String {
        return mod + if (versionPredicates.isEmpty()) "" else " $versionPredicatesString"
    }

    companion object {
        fun of(condition: Condition): ModPredicate {
            require(condition.type == Condition.Type.MOD) { "Only MOD condition type is accepted" }
            return ModPredicate(condition.value, condition.versionPredicates.toList())
        }
    }
}