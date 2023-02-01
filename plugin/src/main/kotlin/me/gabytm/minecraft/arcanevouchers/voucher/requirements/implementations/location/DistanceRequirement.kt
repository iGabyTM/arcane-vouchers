package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.location

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirement
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable.DoubleVariable
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable.LocationVariable
import me.gabytm.minecraft.util.requirements.Arguments
import org.bukkit.entity.Player
import kotlin.math.pow

class DistanceRequirement(
    name: String,
    optional: Boolean,
    negated: Boolean,
    failActions: List<ArcaneAction>,
    actionManager: ArcaneActionManager,
    private val locationVariable: LocationVariable,
    private val distanceVariable: DoubleVariable
) : ArcaneRequirement(
    name, optional, negated, failActions, actionManager
) {

    override fun check(player: Player?, arguments: Arguments): Boolean {
        if (player == null) {
            return false
        }

        val distance = distanceVariable.get(player) ?: kotlin.run {
            distanceVariable.warn(player, getName())
            return false
        }
        val location = locationVariable.get(player) ?: kotlin.run {
            locationVariable.warn(player, getName())
            return false
        }

        if (player.world != location.world) {
            return false
        }

        return (player.location.distanceSquared(location) <= distance.pow(2)) == !negated
    }

    companion object {

        const val TYPE: String = "distance"

    }

}