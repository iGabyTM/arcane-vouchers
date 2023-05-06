package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.worldguard

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.compatibility.worldguard.WorldGuardCompatibility
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirement
import me.gabytm.minecraft.util.requirements.Arguments
import org.bukkit.entity.Player
import java.util.*

class WorldGuardRequirement(
    name: String,
    optional: Boolean,
    negated: Boolean,
    failActions: List<ArcaneAction>,
    actionManager: ArcaneActionManager,
    private val worldGuard: WorldGuardCompatibility,
    private val operation: Operation
) : ArcaneRequirement(
    name, optional, negated, failActions, actionManager
) {

    override fun check(player: Player?, arguments: Arguments): Boolean {
        return player != null && operation.check(player, worldGuard, negated)
    }

    @Suppress("unused")
    enum class Operation(val identifier: String) {

        REGION_OWNER("WorldGuard - region owner") {
            override fun check(player: Player, worldGuard: WorldGuardCompatibility, isNegated: Boolean): Boolean {
                return worldGuard.isOwner(player.location, player) != isNegated
            }
        },

        REGION_MEMBER("WorldGuard - region member") {
            override fun check(player: Player, worldGuard: WorldGuardCompatibility, isNegated: Boolean): Boolean {
                return worldGuard.isMember(player.location, player) != isNegated
            }
        };

        abstract fun check(player: Player, worldGuard: WorldGuardCompatibility, isNegated: Boolean): Boolean

        companion object {

            private val VALUES = EnumSet.allOf(Operation::class.java)

            fun find(string: String): Operation? {
                return VALUES.firstOrNull { it.identifier.equals(string, true) }
            }

        }

    }

}