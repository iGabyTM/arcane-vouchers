package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable

import me.gabytm.minecraft.arcanevouchers.functions.papi
import me.gabytm.minecraft.arcanevouchers.functions.warning
import org.bukkit.entity.Player

data class DoubleVariable(
    private val string: String
) : Variable<Double>(
    { player -> string.toDoubleOrNull() ?: string.papi(player).toDoubleOrNull() }
) {

    override fun warn(player: Player?, requirement: String) {
        warning("[$requirement] '$string' (${string.papi(player)}) is not a valid Double")
    }

}