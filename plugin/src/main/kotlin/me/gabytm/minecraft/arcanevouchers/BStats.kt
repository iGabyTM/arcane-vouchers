package me.gabytm.minecraft.arcanevouchers

import org.bstats.bukkit.Metrics
import org.bstats.charts.DrilldownPie
import org.bstats.charts.SingleLineChart

class BStats(plugin: ArcaneVouchers) {

    // https://bstats.org/plugin/bukkit/ArcaneVouchers/7199
    private val metrics = Metrics(plugin, 7199)

    init {
        metrics.addCustomChart(DrilldownPie("actions") {
            val actions = plugin.voucherManager.getVouchers()
                .flatMap { it.actions }
                .map { it.getName() }
                .groupingBy { it }
                .eachCount()
            mutableMapOf("actions" to actions)
        })

        metrics.addCustomChart(SingleLineChart("vouchers", plugin.voucherManager.getVoucherIds()::size))
    }

}