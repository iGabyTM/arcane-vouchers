package me.gabytm.minecraft.arcanevouchers.utils

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart

class BStats(plugin: ArcaneVouchers) {

    // https://bstats.org/plugin/bukkit/ArcaneVouchers/7199
    private val id = 7199

    private val metrics = Metrics(plugin, id)

    init {
        metrics.addCustomChart(SingleLineChart("vouchers", plugin.voucherManager.getVoucherIds()::size))
    }

}