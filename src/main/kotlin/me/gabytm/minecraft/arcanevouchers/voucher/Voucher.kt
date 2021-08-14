package me.gabytm.minecraft.arcanevouchers.voucher

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class Voucher(
    val settings: VoucherSettings,
    val item: ItemStack,
    val actions: List<ArcaneAction>
) {

    companion object {

        const val NBT = "ArcaneVouchers"

        // TODO: 14-Aug-21 create the item based on config options 
        fun from(config: ConfigurationSection, actionManager: ArcaneActionManager): Voucher {
            val settings = VoucherSettings.from(config.getConfigurationSection("settings"))
            val actions = actionManager.parseActions(config.getStringList("actions"))
            return Voucher(settings, ItemStack(Material.STONE), actions)
        }

    }

}