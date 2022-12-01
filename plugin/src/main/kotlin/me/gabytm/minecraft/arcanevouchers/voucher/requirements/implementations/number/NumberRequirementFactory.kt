package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.number

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirementFactory
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.DoubleVariable
import org.bukkit.configuration.ConfigurationSection

class NumberRequirementFactory : ArcaneRequirementFactory<NumberRequirement>() {

    override fun matches(source: ConfigurationSection): Boolean {
        var type = source.getString("type")?.trim() ?: return false

        if (type.startsWith('!')) {
            type = type.substring(1)
        }

        return NumberRequirement.Operation.find(type) != null
    }

    override fun create(source: ConfigurationSection, actionManager: ArcaneActionManager): NumberRequirement? {
        var type = source.getString("type")?.trim() ?: return null
        val negated = type.startsWith('!')
        val optional = source.getBoolean("optional")

        if (negated) {
            type = type.substring(1)
        }

        val operation = NumberRequirement.Operation.find(type) ?: return null
        val left = DoubleVariable(source.getString("left") ?: return null)
        val right = DoubleVariable(source.getString("right") ?: return null)
        val failActions = actionManager.parseActions(source.getStringList("failActions"))
        return NumberRequirement(source.name, optional, negated, failActions, actionManager, left, right, operation)
    }

}