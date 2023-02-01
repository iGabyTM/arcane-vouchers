package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.number

import me.gabytm.minecraft.arcanevouchers.Constant.Requirement
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirementFactory
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable.DoubleVariable
import org.bukkit.configuration.ConfigurationSection

class NumberRequirementFactory : ArcaneRequirementFactory<NumberRequirement>() {

    private fun warnMissingProperty(
        operation: NumberRequirement.Operation,
        source: ConfigurationSection,
        property: String
    ) {
        warning("Could not load '${operation.identifier}' requirement from ${source.currentPath}: missing required property '${property}'")
    }

    override fun matches(source: ConfigurationSection): Boolean {
        var type = source.getString(Requirement.TYPE)?.trim() ?: return false

        if (type.startsWith(Requirement.NEGATION)) {
            type = type.substring(1)
        }

        return NumberRequirement.Operation.find(type) != null
    }

    override fun create(source: ConfigurationSection, actionManager: ArcaneActionManager): NumberRequirement? {
        var type = source.getString(Requirement.TYPE)?.trim() ?: return null
        val negated = type.startsWith(Requirement.NEGATION)
        val optional = source.getBoolean(Requirement.OPTIONAL)

        if (negated) {
            type = type.substring(1)
        }

        // Not adding a warning for this because it won't get to this point if the factory couldn't find an Operation
        val operation = NumberRequirement.Operation.find(type) ?: return null
        val left = DoubleVariable(
            source.getString("left") ?: kotlin.run {
                warnMissingProperty(operation, source, "left")
                return null
            }
        )
        val right = DoubleVariable(
            source.getString("right") ?: kotlin.run {
                warnMissingProperty(operation, source, "right")
                return null
            }
        )
        val failActions = actionManager.parseActions(source.getStringList(Requirement.FAIL_ACTIONS))
        return NumberRequirement(source.name, optional, negated, failActions, actionManager, left, right, operation)
    }

}