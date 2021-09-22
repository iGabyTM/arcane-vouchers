package me.gabytm.minecraft.arcanevouchers.voucher

import de.tr7zw.nbtapi.NBTItem
import me.gabytm.minecraft.arcanevouchers.Constant.NBT
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.items.ItemCreator
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

class Voucher private constructor(
    val id: String,
    val settings: VoucherSettings,
    val item: ItemStack,
    val actions: List<ArcaneAction>
) {

    companion object {

        fun from(config: ConfigurationSection, actionManager: ArcaneActionManager, itemCreator: ItemCreator): Voucher {
            val id = config.name
            val settings = VoucherSettings.from(config.getConfigurationSection("settings"))
            val item = itemCreator.create(true, config.getConfigurationSection("item"), Material.PAPER).apply {
                val nbtItem = NBTItem(this)
                val compound = nbtItem.getCompound(NBT.VOUCHER_COMPOUND)

                compound.addCompound(NBT.ARGUMENTS_COMPOUND)
                compound.setString(NBT.VOUCHER_NAME, config.name)
                nbtItem.item
            }
            val actions = actionManager.parseActions(config.getStringList("actions"))

            return Voucher(id, settings, item, actions)
        }

    }

}