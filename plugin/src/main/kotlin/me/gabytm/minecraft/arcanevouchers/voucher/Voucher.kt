package me.gabytm.minecraft.arcanevouchers.voucher

import de.tr7zw.nbtapi.NBT
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneActionManager
import me.gabytm.minecraft.arcanevouchers.functions.add
import me.gabytm.minecraft.arcanevouchers.functions.audience
import me.gabytm.minecraft.arcanevouchers.functions.debug
import me.gabytm.minecraft.arcanevouchers.functions.item
import me.gabytm.minecraft.arcanevouchers.items.ItemCreator
import me.gabytm.minecraft.arcanevouchers.limit.LimitType
import me.gabytm.minecraft.arcanevouchers.voucher.requirements.ArcaneRequirementProcessor
import me.gabytm.minecraft.arcanevouchers.voucher.settings.VoucherSettings
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Voucher private constructor(
    val id: String,
    val settings: VoucherSettings,
    val item: ItemStack,
    val itemName: String,
    val itemLore: List<String>,
    val actions: List<ArcaneAction>,
    val bulkActions: List<ArcaneAction>
) {

    private fun handleBulkOpenLimit(
        player: Player, voucher: ItemStack,
        args: MutableMap<String, String>, plugin: ArcaneVouchers,
        increaseLimit: Boolean
    ) {
        val vouchers = voucher.amount
        val bulkLimit = this.settings.bulkOpen.limit

        debug(player, "[redeem] bulkLimit = $bulkLimit, vouchers = $vouchers")

        // If bulkOpen limit is higher than the amount of vouchers, so the player can redeem them all
        val amount = if (bulkLimit >= vouchers) {
            vouchers
        } else {
            // Otherwise, they can redeem only 'bulkLimit' vouchers
            bulkLimit
        }
        redeem(player, voucher, args, plugin, increaseLimit, true, amount)
    }

    private fun redeem(
        player: Player, voucher: ItemStack, args: MutableMap<String, String>,
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

        with(player.audience()) {
            settings.messages.redeemMessage.send(this, args.add("{amount}", amount.toString()))
            settings.sounds.redeemSound.play(this)
        }
    }

    private fun executeActions(
        player: Player, amount: Int,
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
            player.item(ItemStack(Material.AIR))
            return
        }

        // Otherwise, subtract one
        voucher.amount -= amount
        // FIXME for some reason, when confirmation is enabled, on certain game versions the item is not updated by the
        //       code that's above this line, a workaround I found is to set player's item in hand
        player.item(voucher)
    }

    fun redeem(
        player: Player,
        voucher: ItemStack,
        args: MutableMap<String, String>,
        plugin: ArcaneVouchers,
        isBulk: Boolean
    ) {
        val limitManager = plugin.voucherManager.limitManager
        val vouchers = voucher.amount
        val limit = this.settings.limit
        val hasLimit = limit.enabled && limit.type != LimitType.NONE

        if (hasLimit) {
            debug(player, "[redeem] voucher $id has ${limit.type} limit of ${limit.limit}")
        }

        if (isBulk) {
            // The player bypasses the limit
            if (limitManager.bypassLimit(player, this)) {
                debug(player, "[redeem] (bulk) bypass the limit for $id")
                handleBulkOpenLimit(player, voucher, args, plugin, false)
                return
            }

            // If the voucher has a limit of usages, we need to check how many vouchers they can redeem
            if (hasLimit) {
                val usages = limitManager.getUsages(player.uniqueId, this)
                // Calculate the usages left
                val usagesLeft = (limit.limit - usages).toInt()

                debug(player, "[redeem] (bulk) usages = $usages, usagesLeft = $usagesLeft")

                // The player has enough usagesLeft to redeem all vouchers
                if (usagesLeft >= vouchers) {
                    handleBulkOpenLimit(player, voucher, args, plugin, true)
                }
                return
            }

            // Otherwise, go straight to redeem
            handleBulkOpenLimit(player, voucher, args, plugin, true)
            return
        }

        // The player bypass the limit
        if (limitManager.bypassLimit(player, this)) {
            redeem(player, voucher, args, plugin, increaseLimit = false, isBulk = false, 1)
            return
        }

        // If the voucher has a limit of usages, we need to check how many vouchers they can redeem
        if (hasLimit) {
            // Get how many times the player has used this voucher
            val usages = limitManager.getUsages(player.uniqueId, this)
            // Calculate the usages left
            val usagesLeft = (limit.limit - usages).toInt()

            debug(player, "[redeem] usages = $usages, usagesLeft = $usagesLeft")

            // The player can redeem more than 1 voucher
            if (usagesLeft >= 1) {
                redeem(player, voucher, args, plugin, increaseLimit = true, isBulk = false, 1)
            }
            return
        }

        // Otherwise, go straight to redeem
        redeem(player, voucher, args, plugin, increaseLimit = true, isBulk = false, 1)
    }

    companion object {

        fun from(
            config: ConfigurationSection, actionManager: ArcaneActionManager,
            itemCreator: ItemCreator, requirementProcessor: ArcaneRequirementProcessor
        ): Voucher {
            val id = config.name
            val settings = VoucherSettings.from(config.getConfigurationSection("settings"), requirementProcessor)
            val item = itemCreator.create(true, config.getConfigurationSection("item"), Material.PAPER)

            NBT.modify(item) { itemNbt ->
                val compound = itemNbt.getOrCreateCompound(Constant.NBT.VOUCHER_COMPOUND)

                compound.resolveOrCreateCompound(Constant.NBT.ARGUMENTS_COMPOUND)
                compound.setString(Constant.NBT.VOUCHER_NAME, id)
            }

            val actions = actionManager.parseActions(config.getStringList("actions"))
            val bulkActions = actionManager.parseActions(config.getStringList("bulkActions"))
            return Voucher(
                id,
                settings,
                item,
                config.getString("item.name") ?: "",
                config.getStringList("item.lore"),
                actions,
                bulkActions
            )
        }

    }

}