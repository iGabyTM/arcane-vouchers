package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.string

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.functions.papi
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirement
import me.gabytm.minecraft.util.requirements.Arguments
import org.bukkit.entity.Player
import java.util.*

class StringRequirement(
    name: String,
    optional: Boolean,
    negated: Boolean,
    failActions: List<ArcaneAction>,
    actionManager: ArcaneActionManager,
    private val left: String,
    private val right: String,
    private val operation: Operation
) : ArcaneRequirement(
    name, optional, negated, failActions, actionManager
) {

    override fun check(player: Player?, arguments: Arguments): Boolean {
        return operation.check(left.papi(player), right.papi(player), !negated)
    }

    @Suppress("unused")
    enum class Operation(private val identifier: String) {

        EQUALS("string equals") {
            override fun check(left: String?, right: String?, negated: Boolean): Boolean {
                return left?.equals(right) == negated
            }
        },

        EQUALS_IGNORE_CASE("string equals ignore case") {
            override fun check(left: String?, right: String?, negated: Boolean): Boolean {
                return left?.equals(right, true) == negated
            }
        },

        CONTAINS("string contains") {
            override fun check(left: String?, right: String?, negated: Boolean): Boolean {
                return right != null && left?.contains(right) == negated
            }
        },

        CONTAINS_IGNORE_CASE("string contains ignore case") {
            override fun check(left: String?, right: String?, negated: Boolean): Boolean {
                return right != null && left?.contains(right, true) == negated
            }
        };

        abstract fun check(left: String?, right: String?, negated: Boolean): Boolean

        companion object {

            private val VALUES = EnumSet.allOf(Operation::class.java)

            fun find(string: String): Operation? {
                return VALUES.firstOrNull { it.identifier.equals(string, true) }
            }

        }

    }

}