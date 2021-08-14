package me.gabytm.minecraft.arcanevouchers.voucher

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers

class VoucherManager(private val plugin: ArcaneVouchers) {

    private val loadedVouchers = mutableMapOf<String, Voucher>()

    fun loadVouchers() {
        loadedVouchers.clear()

        val vouchersSection = plugin.vouchersConfig.getSection("vouchers") ?: kotlin.run {
            plugin.logger.warning("Could not find the 'vouchers' section")
            return
        }

        for (it in vouchersSection.getValues(false).keys) {
            val voucherSection = vouchersSection.getConfigurationSection(it) ?: continue

        }
    }

}