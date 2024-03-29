package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.string

import me.gabytm.minecraft.arcanevouchers.Constant.Requirement
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirementFactory
import org.bukkit.configuration.ConfigurationSection

class StringRequirementFactory : ArcaneRequirementFactory<StringRequirement>() {

    private fun warnMissingProperty(
        operation: StringRequirement.Operation,
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

        return StringRequirement.Operation.find(type) != null
    }

    override fun create(source: ConfigurationSection, actionManager: ArcaneActionManager): StringRequirement? {
        var type = source.getString(Requirement.TYPE)?.trim() ?: return null
        val negated = type.startsWith(Requirement.TYPE)
        val optional = source.getBoolean(Requirement.OPTIONAL)

        if (negated) {
            type = type.substring(1)
        }

        val operation = StringRequirement.Operation.find(type) ?: return null
        val left = source.getString("left") ?: kotlin.run {
            warnMissingProperty(operation, source, "left")
            return null
        }
        val right = source.getString("right") ?: kotlin.run {
            warnMissingProperty(operation, source, "right")
            return null
        }
        val failActions = actionManager.parseActions(source.getStringList(Requirement.FAIL_ACTIONS))
        return StringRequirement(source.name, optional, negated, failActions, actionManager, left, right, operation)
    }

}