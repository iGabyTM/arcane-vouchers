package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable

import me.gabytm.minecraft.arcanevouchers.functions.papi

data class BooleanVariable(
    private val stringValue: String
) : Variable<Boolean>(
    { player -> stringValue.toBooleanStrictOrNull() ?: stringValue.papi(player).toBooleanStrictOrNull() }
)
