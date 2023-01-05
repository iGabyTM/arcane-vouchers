package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common

import me.gabytm.minecraft.arcanevouchers.functions.papi
import org.bukkit.entity.Player

data class DoubleVariable(private val stringValue: String) {

    private val doubleValue = stringValue.toDoubleOrNull()

    fun get(player: Player?): Double? {
        return doubleValue ?: stringValue.papi(player).toDoubleOrNull()
    }

}