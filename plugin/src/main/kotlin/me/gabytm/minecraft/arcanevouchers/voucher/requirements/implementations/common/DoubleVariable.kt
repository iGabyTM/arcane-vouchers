package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common

import me.gabytm.minecraft.arcanevouchers.functions.papi
import org.bukkit.entity.Player

class DoubleVariable(val stringValue: String) {

    private val numericValue = stringValue.toDoubleOrNull()

    fun get(player: Player?): Double? {
        return numericValue ?: stringValue.papi(player).toDoubleOrNull()
    }

}