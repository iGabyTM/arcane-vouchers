package me.gabytm.minecraft.arcanevouchers.listeners

import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTItem
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.NBT
import me.gabytm.minecraft.arcanevouchers.functions.component1
import me.gabytm.minecraft.arcanevouchers.functions.component2
import me.gabytm.minecraft.arcanevouchers.functions.add
import me.gabytm.minecraft.arcanevouchers.limit.LimitType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class VoucherUseListener(private val plugin: ArcaneVouchers) : Listener {

    private val voucherManager = plugin.voucherManager
    private val limitManager = voucherManager.limitManager
    private val audiences = plugin.audiences

    private fun NBTCompound.getArgs(): MutableMap<String, String> {
        return keys.associateBy { this.getString(it) }.toMutableMap()
    }

    @EventHandler
    fun PlayerInteractEvent.onEvent() {
        val item = this.item ?: return

        // If the item doesn't have item meta therefore can't be a voucher
        if (!item.hasItemMeta()) {
            return
        }

        val nbt = NBTItem(item)

        // The item doesn't have the NBT compound
        if (!nbt.hasKey(NBT.VOUCHER_COMPOUND)) {
            return
        }

        val compound = nbt.getCompound(NBT.VOUCHER_COMPOUND)
        val voucherId = compound.getString(NBT.VOUCHER_NAME)
        val voucher = voucherManager.getVoucher(voucherId) ?: return // it was probably removed from config
        val settings = voucher.settings

        this.isCancelled = true

        val args = compound.getCompound(NBT.ARGUMENTS_COMPOUND).getArgs()
        val (placeholders, values) = args

        val receiverName: String? = compound.getString(NBT.RECEIVER_NAME)

        val audience = audiences.player(this.player)

        // Bind to receiver is enabled
        if (settings.bindToReceiver.enabled) {
            // If the receiver name is null it means that this voucher was crated before v2.0.0
            if (receiverName != null && receiverName != this.player.name) {
                settings.bindToReceiver.message.send(audience, args.add("{player}", receiverName))
                return
            }
        }

        val limit = settings.limit

        // Limit is enabled
        if (limit.enabled && limit.type != LimitType.NONE) {
            val usages = limitManager.getUsages(player.uniqueId, voucher)

            // The limit was reached and the player can't bypass it
            if (usages >= limit.limit && !limitManager.bypassLimit(player, voucher)) {
                limit.message.send(audience, args)
                return
            }
        }

        val world = this.player.world

        // Player's world is blacklisted
        if (settings.worlds.isBlacklisted(world, placeholders, values)) {
            settings.worlds.blacklistedMessage.send(audience, args.add("{world}", world.name))
            return
        }

        // Player's world is not whitelisted
        if (!settings.worlds.isWhitelisted(world, placeholders, values)) {
            settings.worlds.notWhitelistedMessage.send(audience, args.add("{world}", world.name))
            return
        }

        // The player is blacklisted by permission
        if (settings.permissions.isBlacklisted(this.player, placeholders, values)) {
            settings.permissions.blacklistedMessage.send(audience, args)
            return
        }

        // The player is not whitelisted
        if (!settings.permissions.isWhitelisted(this.player, placeholders, values)) {
            settings.permissions.notWhitelistedMessage.send(audience, args)
            return
        }
    }

}