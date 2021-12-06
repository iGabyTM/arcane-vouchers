package me.gabytm.minecraft.arcanevouchers.voucher

import de.tr7zw.nbtapi.NBTItem
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.NBT
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.items.ItemCreator
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Voucher private constructor(
    val id: String,
    val settings: VoucherSettings,
    val item: ItemStack,
    val actions: List<ArcaneAction>,
    val bulkActions: List<ArcaneAction>
) {

    fun redeem(player: Player, voucher: ItemStack, plugin: ArcaneVouchers, isBulk: Boolean) {
        val limitManager = plugin.voucherManager.limitManager
        val actionManager = plugin.actionManager
        val amount = voucher.amount

        this.settings.messages.redeemMessage.send(plugin.audiences.player(player))

        if (isBulk) {
            if (limitManager.bypassLimit(player, this)) {
                actionManager.executeActions(player, this.bulkActions, mutableMapOf("{amount}" to amount.toString()))
                voucher.type = Material.AIR
                return
            }

            val usages = limitManager.getUsages(player.uniqueId, this)
            val usagesLeft = (this.settings.limit.limit - usages).toInt()

            if (usagesLeft >= amount) {
                limitManager.increaseLimit(player.uniqueId, this.id, amount)
                actionManager.executeActions(player, this.bulkActions, mutableMapOf("{amount}" to amount.toString()))
                voucher.type = Material.AIR
                return
            }

            limitManager.increaseLimit(player.uniqueId, this.id, usagesLeft)
            actionManager.executeActions(player, this.bulkActions, mutableMapOf("{amount}" to usagesLeft.toString()))
            item.amount = amount - usagesLeft
            return
        }

        if (limitManager.bypassLimit(player, this)) {
            actionManager.executeActions(player, this.actions)

            if (amount == 1) {
                item.type = Material.AIR
            } else {
                item.amount = amount - 1
            }
            return
        }

        val usages = limitManager.getUsages(player.uniqueId, this)
        val usagesLeft = (this.settings.limit.limit - usages).toInt()

        if (usagesLeft >= amount) {
            limitManager.increaseLimit(player.uniqueId, this.id, amount)
            actionManager.executeActions(player, this.actions)
            item.type = Material.AIR
            return
        }

        limitManager.increaseLimit(player.uniqueId, this.id, usagesLeft)
        actionManager.executeActions(player, this.actions)
        item.amount = amount - usagesLeft
    }

    companion object {

        fun from(config: ConfigurationSection, actionManager: ArcaneActionManager, itemCreator: ItemCreator): Voucher {
            val id = config.name
            val settings = VoucherSettings.from(config.getConfigurationSection("settings"))
            val item = itemCreator.create(true, config.getConfigurationSection("item"), Material.PAPER).apply {
                val nbtItem = NBTItem(this, true)
                val compound = nbtItem.getOrCreateCompound(NBT.VOUCHER_COMPOUND)

                compound.addCompound(NBT.ARGUMENTS_COMPOUND)
                compound.setString(NBT.VOUCHER_NAME, config.name)
                nbtItem.item
            }

            val actions = actionManager.parseActions(config.getStringList("actions"))
            val bulkActions = actionManager.parseActions(config.getStringList("bulkActions"))
            return Voucher(id, settings, item, actions, bulkActions)
        }

    }

}