package me.gabytm.minecraft.arcanevouchers.voucher.requirements.implementations.common.variable

import org.bukkit.entity.Player

abstract class Variable<T>(
    @Transient private val transformer: (player: Player?) -> T?
) {

    @Suppress("unused")
    private val variableType = this::class.java.simpleName
    private val parsed: T? = transformer(null)

    fun get(player: Player?): T? = parsed ?: transformer(player)

    abstract fun warn(player: Player?, requirement: String)

}