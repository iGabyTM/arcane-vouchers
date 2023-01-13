package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable

import me.gabytm.minecraft.arcanevouchers.functions.papi
import me.gabytm.minecraft.arcanevouchers.functions.warning
import org.bukkit.entity.Player

data class BooleanVariable(
    private val string: String
) : Variable<Boolean>(
    { player -> string.toBooleanStrictOrNull() ?: string.papi(player).toBooleanStrictOrNull() }
) {

    override fun warn(player: Player?, requirement: String) {
        warning("[$requirement] '$string' (${string.papi(player)}) is not a valid boolean (accepted values: true, false)")
    }

}
