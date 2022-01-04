package me.gabytm.minecraft.arcanevouchers.listeners

import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTItem
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.NBT
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.functions.add
import me.gabytm.minecraft.arcanevouchers.functions.component1
import me.gabytm.minecraft.arcanevouchers.functions.component2
import me.gabytm.minecraft.arcanevouchers.functions.item
import me.gabytm.minecraft.arcanevouchers.limit.LimitType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

class VoucherUseListener(private val plugin: ArcaneVouchers) : Listener {

    private val useActions = EnumSet.of(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)
    private val voucherManager = plugin.voucherManager
    private val limitManager = voucherManager.limitManager
    private val compatHandler = plugin.compatHandler
    private val audiences = plugin.audiences

    private fun NBTCompound.getArgs(): MutableMap<String, String> {
        return keys.associateBy { this.getString(it) }.toMutableMap()
    }

    @EventHandler
    fun PlayerInteractEvent.onEvent() {
        if (!useActions.contains(this.action)) {
            return
        }

        if (ServerVersion.HAS_OFF_HAND && this.hand != EquipmentSlot.HAND) {
            return
        }

        val item = this.player.item()

        if (item.type == Material.AIR) {
            return
        }

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

        // TODO: 12/6/2021 finish API
/*        val event = VoucherRedeemEvent(this.player, voucher)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }*/

        this.isCancelled = true

        val args = compound.getCompound(NBT.ARGUMENTS_COMPOUND).getArgs()
        val (placeholders, values) = args

        val audience = audiences.player(this.player)

        val bindToReceiver = settings.bindToReceiver

        // Bind to receiver is enabled
        if (bindToReceiver.enabled) {
            val receiverUuid: UUID? = compound.getUUID(NBT.RECEIVER_UUID)

            // If the receiver name is null it means that this voucher was crated before v2.0.0
            if (receiverUuid != null && receiverUuid != this.player.uniqueId) {
                bindToReceiver.message.send(audience, args.add("{player}", Bukkit.getOfflinePlayer(receiverUuid).name ?: ""))
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

        // The voucher has a cooldown set
        if (settings.cooldown.has()) {
            val timeLeft = voucherManager.cooldownManager.getTimeLeft(player.uniqueId, voucher)

            // The player is on cooldown, and they can't bypass it
            if (timeLeft > 0L && !voucherManager.cooldownManager.bypassCooldown(player, voucherId)) {
                // TODO: 1/4/2022 format time left
                settings.cooldown.message.send(audience, args.add("{left}","${timeLeft / 1000}s"))
                return
            }
        }

        val world = this.player.world
        val worlds = settings.worlds

        // Player's world is blacklisted
        if (worlds.isBlacklisted(world, placeholders, values)) {
            worlds.blacklistedMessage.send(audience, args.add("{world}", world.name))
            return
        }

        // Player's world is not whitelisted
        if (!worlds.isWhitelisted(world, placeholders, values)) {
            worlds.notWhitelistedMessage.send(audience, args.add("{world}", world.name))
            return
        }

        if (compatHandler.hasWorldGuardSupport) {
            val regions = settings.regions
            val worldGuardRegions = compatHandler.worldGuardCompat.getRegions(player.location)

            // The player is inside a blacklisted region
            if (regions.isBlacklisted(worldGuardRegions, placeholders, values)) {
                regions.blacklistedMessage.send(audience, args)
                return
            }

            // The player is not inside a whitelisted region
            if (!regions.isWhitelisted(worldGuardRegions, placeholders, values)) {
                regions.notWhitelistedMessage.send(audience, args)
                return
            }
        }

        val permissions = settings.permissions

        // The player is blacklisted by permission
        if (permissions.isBlacklisted(this.player, placeholders, values)) {
            permissions.blacklistedMessage.send(audience, args)
            return
        }

        // The player is not whitelisted
        if (!permissions.isWhitelisted(this.player, placeholders, values)) {
            permissions.notWhitelistedMessage.send(audience, args)
            return
        }

        val isBulk = settings.bulkOpen.enabled && player.isSneaking && settings.cooldown.allowBulkOpen

        if (settings.confirmationEnabled) {
            voucherManager.openConfirmation(player, voucher, item, isBulk)
        } else {
            voucher.redeem(player, item, plugin, isBulk)
        }
    }

}