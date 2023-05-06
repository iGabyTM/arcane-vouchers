package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.worldguard

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.compatibility.worldguard.WorldGuardCompatibility
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirementFactory
import org.bukkit.configuration.ConfigurationSection

class WorldGuardRequirementFactory(
    private val worldGuard: WorldGuardCompatibility
) : ArcaneRequirementFactory<WorldGuardRequirement>() {

    override fun matches(source: ConfigurationSection): Boolean {
        var type = source.getString(Constant.Requirement.TYPE)?.trim() ?: return false

        if (type.startsWith(Constant.Requirement.NEGATION)) {
            type = type.substring(1)
        }

        return WorldGuardRequirement.Operation.find(type) != null
    }

    override fun create(source: ConfigurationSection, actionManager: ArcaneActionManager): WorldGuardRequirement? {
        var type = source.getString(Constant.Requirement.TYPE)?.trim() ?: return null
        val negated = type.startsWith(Constant.Requirement.TYPE)
        val optional = source.getBoolean(Constant.Requirement.OPTIONAL)

        if (negated) {
            type = type.substring(1)
        }

        val operation = WorldGuardRequirement.Operation.find(type) ?: return null
        val failActions = actionManager.parseActions(source.getStringList(Constant.Requirement.FAIL_ACTIONS))
        return WorldGuardRequirement(source.name, optional, negated, failActions, actionManager, worldGuard, operation)
    }

}