package dev.kikugie.techutils.client.util.condition

import me.fallenbreath.conditionalmixin.api.annotation.Condition
import me.fallenbreath.conditionalmixin.api.annotation.Restriction


class ModRestriction private constructor(
    val requirements: List<ModPredicate>,
    val conflicts: List<ModPredicate>,
    val requirementsSatisfied: Boolean,
    val noConflict: Boolean
) {
    companion object {
        fun of(restriction: Restriction, conditionPredicate: (Condition) -> Boolean): ModRestriction {
            val requirements = generateRequirement(restriction.require, conditionPredicate)
            val conflictions = generateRequirement(restriction.conflict, conditionPredicate)
            val requirementsSatisfied = requirements.all(ModPredicate::isSatisfied)
            val noConfliction = conflictions.none(ModPredicate::isSatisfied)
            return ModRestriction(requirements, conflictions, requirementsSatisfied, noConfliction)
        }

        fun of(restriction: Restriction): ModRestriction {
            return of(restriction) { true }
        }

        private fun generateRequirement(
            conditions: Array<Condition>,
            conditionPredicate: (Condition) -> Boolean
        ): List<ModPredicate> {
            return conditions
                .filter { it.type == Condition.Type.MOD }
                .filter(conditionPredicate)
                .map(ModPredicate::of)
        }
    }
}
