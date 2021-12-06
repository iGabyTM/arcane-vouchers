package me.gabytm.minecraft.arcanevouchers.api.events

import me.gabytm.minecraft.arcanevouchers.voucher.Voucher
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

// TODO: 12/6/2021 finish API
class VoucherRedeemEvent(
    public val player: Player,
    public val voucher: Voucher
) : Event(), Cancellable {

    private var cancelled: Boolean = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean = this.cancelled

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

    companion object {

        private val handlerList = HandlerList()

        fun getHandlerList(): HandlerList = handlerList

    }

}