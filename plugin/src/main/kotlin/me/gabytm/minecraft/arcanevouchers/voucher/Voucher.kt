package me.gabytm.minecraft.arcanevouchers.voucher

import de.tr7zw.nbtapi.NBTItem
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.NBT
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.functions.add
import me.gabytm.minecraft.arcanevouchers.functions.audience
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

    private fun redeem(player: Player, voucher: ItemStack, args: MutableMap<String, String>,
                       plugin: ArcaneVouchers, increaseLimit: Boolean,
                       isBulk: Boolean, amount: Int
    ) {
        val voucherManager = plugin.voucherManager

        if (increaseLimit) {
            plugin.voucherManager.limitManager.increaseUsages(player.uniqueId, this.id, amount.toLong())
        }

        executeActions(player, amount, args, isBulk, plugin.actionManager)
        removeVouchers(player, voucher, amount)

        // Set the voucher on cooldown if it is enabled and the player can't bypass it
        if (this.settings.cooldown.enabled && !voucherManager.cooldownManager.bypassCooldown(player, this.id)) {
            voucherManager.cooldownManager.addCooldown(player.uniqueId, this.id, this.settings.cooldown.cooldown)
        }

        this.settings.messages.redeemMessage.send(player.audience(), args.add("{amount}", amount.toString()))
    }

    private fun executeActions(player: Player, amount: Int,
                               args: MutableMap<String, String>, isBulk: Boolean,
                               actionManager: ArcaneActionManager
    ) {
        if (isBulk) {
            // Execute actions n times (n = amount) if bulkActions is empty
            if (this.bulkActions.isEmpty()) {
                for (i in 1..amount) {
                    actionManager.executeActions(player, this.actions, args)
                }
                return
            }

            actionManager.executeActions(player, this.bulkActions, args.add("%amount%", amount.toString()))
            return
        }

        actionManager.executeActions(player, this.actions, args)
    }

    private fun removeVouchers(player: Player, voucher: ItemStack, amount: Int) {
        // Remove the item completely if it has the same amount as the amount of redeemed vouchers
        if (voucher.amount == amount) {
            // On 1.11.2+ we can just set the item amount to 0, and it will be removed
            if (ServerVersion.ITEMS_WITH_ZERO_AMOUNT_ARE_REMOVED) {
                voucher.amount = 0
            } else {
                // But on pre 1.11.2 that doesn't work
                player.setItemInHand(null)
            }

            return
        }

        // Otherwise, subtract one
        voucher.amount = voucher.amount - amount
    }

    fun redeem(player: Player, voucher: ItemStack, args: MutableMap<String, String>, plugin: ArcaneVouchers, isBulk: Boolean) {
        val limitManager = plugin.voucherManager.limitManager
        val vouchers = voucher.amount

        if (isBulk) {
            // The player bypasses the limit
            if (limitManager.bypassLimit(player, this)) {
                redeem(player, voucher, args, plugin, increaseLimit = false, isBulk = true, vouchers)
                return
            }

            // Get how many times the player has used this voucher
            val usages = limitManager.getUsages(player.uniqueId, this)
            // Calculate the usages left
            val usagesLeft = (this.settings.limit.limit - usages).toInt()

            // The player has more usages left than the amount of vouchers used now
            if (usagesLeft >= vouchers) {
                val bulkLimit = settings.bulkOpen.limit

                // The bulkOpen limit is higher than the amount of vouchers used now
                if (bulkLimit >= vouchers) {
                    redeem(player, voucher, args, plugin, increaseLimit = true, isBulk = true, vouchers)
                    return
                }

                // Redeem only 'bulkLimit' vouchers
                redeem(player, voucher, args, plugin, true, isBulk = true, bulkLimit)
                return
            }

            // Redeem only 'usagesLeft' vouchers
            redeem(player, voucher, args, plugin, true, isBulk = true, usagesLeft)
            return
        }

        // The player bypass the limit
        if (limitManager.bypassLimit(player, this)) {
            redeem(player, voucher, args, plugin, false, isBulk = false, 1)
            return
        }

        // Get how many times the player has used this voucher
        val usages = limitManager.getUsages(player.uniqueId, this)
        // Calculate the usages left
        val usagesLeft = (this.settings.limit.limit - usages).toInt()

        // The player can redeem more than 1 voucher
        if (usagesLeft >= 1) {
            redeem(player, voucher, args, plugin, true, isBulk = false, 1)
        }
    }

    companion object {

        fun from(config: ConfigurationSection, actionManager: ArcaneActionManager, itemCreator: ItemCreator): Voucher {
            val id = config.name
            val settings = VoucherSettings.from(config.getConfigurationSection("settings"))
            val item = itemCreator.create(true, config.getConfigurationSection("item"), Material.PAPER).apply {
                val nbtItem = NBTItem(this, true)
                val compound = nbtItem.getOrCreateCompound(NBT.VOUCHER_COMPOUND)

                compound.addCompound(NBT.ARGUMENTS_COMPOUND)
                compound.setString(NBT.VOUCHER_NAME, id)
                nbtItem.item
            }

            val actions = actionManager.parseActions(config.getStringList("actions"))
            val bulkActions = actionManager.parseActions(config.getStringList("bulkActions"))
            return Voucher(id, settings, item, actions, bulkActions)
        }

    }

}