package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.number

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirement
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable.DoubleVariable
import me.gabytm.minecraft.util.requirements.Arguments
import org.bukkit.entity.Player
import java.util.*

class NumberRequirement(
    name: String,
    optional: Boolean,
    negated: Boolean,
    failActions: List<ArcaneAction>,
    actionManager: ArcaneActionManager,
    private val left: DoubleVariable,
    private val right: DoubleVariable,
    private val operation: Operation
) : ArcaneRequirement(
    name, optional, negated, failActions, actionManager
) {

    override fun check(player: Player?, arguments: Arguments): Boolean {
        val leftNumber = left.get(player) ?: kotlin.run {
            left.warn(player, getName())
            return false
        }
        val rightNumber = right.get(player) ?: kotlin.run {
            right.warn(player, getName())
            return false
        }

        return operation.check(leftNumber, rightNumber, !negated)
    }

    @Suppress("unused")
    enum class Operation(private val identifier: String) {

        EQUAL("==") {
            override fun check(left: Double, right: Double, negated: Boolean): Boolean {
                return (left == right) == negated
            }
        },

        GREATER(">") {
            override fun check(left: Double, right: Double, negated: Boolean): Boolean {
                return (left > right) == negated
            }
        },

        GREATER_OR_EQUAL(">=") {
            override fun check(left: Double, right: Double, negated: Boolean): Boolean {
                return (left >= right) == negated
            }
        },

        SMALLER("<") {
            override fun check(left: Double, right: Double, negated: Boolean): Boolean {
                return (left < right) == negated
            }
        },

        SMALLER_OR_EQUAL("<=") {
            override fun check(left: Double, right: Double, negated: Boolean): Boolean {
                return (left <= right) == negated
            }
        };

        abstract fun check(left: Double, right: Double, negated: Boolean): Boolean

        companion object {

            private val VALUES = EnumSet.allOf(Operation::class.java)

            fun find(string: String): Operation? {
                return VALUES.firstOrNull { it.identifier == string }
            }

        }

    }

}