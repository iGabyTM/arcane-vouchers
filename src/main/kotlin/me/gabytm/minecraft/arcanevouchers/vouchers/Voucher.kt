package me.gabytm.minecraft.arcanevouchers.vouchers

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import org.bukkit.inventory.ItemStack

class Voucher(
    val settings: VoucherSettings,
    val item: ItemStack,
    val actions: List<ArcaneAction>
) {

    companion object {

        const val NBT = "ArcaneVouchers"

    }

}