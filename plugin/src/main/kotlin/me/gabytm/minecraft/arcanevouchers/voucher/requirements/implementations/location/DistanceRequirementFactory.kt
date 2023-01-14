package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.location

import me.gabytm.minecraft.arcanevouchers.Constant.Requirement
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirementFactory
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable.DoubleVariable
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable.LocationVariable
import org.bukkit.configuration.ConfigurationSection

class DistanceRequirementFactory : ArcaneRequirementFactory<DistanceRequirement>() {

    override fun matches(source: ConfigurationSection): Boolean {
        var type = source.getString(Requirement.TYPE)?.trim() ?: return false

        if (type.startsWith(Requirement.NEGATION)) {
            type = type.substring(1)
        }

        return type.equals(DistanceRequirement.TYPE, true)
    }

    override fun create(source: ConfigurationSection, actionManager: ArcaneActionManager): DistanceRequirement? {
        val type = source.getString(Requirement.TYPE)?.trim() ?: return null
        val negated = type.startsWith(Requirement.NEGATION)
        val optional = source.getBoolean(Requirement.OPTIONAL)

        val location = LocationVariable(
            source.getString("location") ?: kotlin.run {
                warning("Could not load 'distance' requirement from ${source.currentPath}: missing required property 'location'")
                return null
            }
        )
        val distance = DoubleVariable(
            source.getString("distance") ?: kotlin.run {
                warning("Could not load 'distance' requirement from ${source.currentPath}: missing required property 'distance'")
                return null
            }
        )
        val failActions = actionManager.parseActions(source.getStringList(Requirement.FAIL_ACTIONS))
        return DistanceRequirement(source.name, optional, negated, failActions, actionManager, location, distance)
    }

}