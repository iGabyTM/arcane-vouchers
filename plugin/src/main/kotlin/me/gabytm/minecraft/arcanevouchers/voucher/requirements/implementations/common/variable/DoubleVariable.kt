package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable

import me.gabytm.minecraft.arcanevouchers.functions.papi

data class DoubleVariable(
    private val string: String
) : Variable<Double>(
    { player -> string.toDoubleOrNull() ?: string.papi(player).toDoubleOrNull() }
)