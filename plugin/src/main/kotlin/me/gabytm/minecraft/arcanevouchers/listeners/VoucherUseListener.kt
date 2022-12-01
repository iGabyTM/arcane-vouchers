package me.gabytm.minecraft.arcanevouchers.listeners

import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTItem
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.NBT
import me.gabytm.minecraft.arcanevouchers.ServerVersion
import me.gabytm.minecraft.arcanevouchers.functions.*
import me.gabytm.minecraft.arcanevouchers.limit.LimitType
import me.gabytm.minecraft.arcanevouchers.voucher.Voucher
import me.gabytm.minecraft.util.requirements.Arguments
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
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
    private val compatibilityHandler = plugin.compatibilityHandler
    private val audiences = plugin.audiences

    private fun NBTCompound.getArgs(): MutableMap<String, String> {
        return keys.associateWith { getString(it) }.toMutableMap()
    }

    private fun hasReachedLimit(player: Player, voucher: Voucher, args: MutableMap<String, String>): Boolean {
        val limit = voucher.settings.limit

        if (!limit.enabled || limit.type == LimitType.NONE) {
            return false;
        }

        val usages = limitManager.getUsages(player.uniqueId, voucher)

        // The limit was reached and the player can't bypass it
        if (usages >= limit.limit && !limitManager.bypassLimit(player, voucher)) {
            val audience = audiences.player(player)
            limit.message.send(audience, args)
            limit.sound.play(audience)
            return true
        }

        return false
    }

    private fun isOnCooldown(player: Player, voucher: Voucher, args: MutableMap<String, String>): Boolean {
        val cooldown = voucher.settings.cooldown

        if (!cooldown.enabled) {
            return false
        }

        val cooldownManager = voucherManager.cooldownManager
        val timeLeft = cooldownManager.getTimeLeft(player.uniqueId, voucher)

        // The player is on cooldown, and they can't bypass it
        if (timeLeft > 0L && !cooldownManager.bypassCooldown(player, voucher.id)) {
            val audience = audiences.player(player)
            cooldown.message.send(audience, args, mapOf("{left}" to cooldownManager.formatTimeLeft(timeLeft)))
            cooldown.sound.play(audience)
            return true
        }

        return false
    }

    private fun isNotAllowedInWorld(player: Player, voucher: Voucher, args: MutableMap<String, String>): Boolean {
        val world = player.world
        val worlds = voucher.settings.worlds
        val (placeholders, values) = args
        val audience = player.audience()

        // Player's world is blacklisted
        if (worlds.isBlacklisted(world, placeholders, values)) {
            worlds.blacklistedMessage.send(audience, args.add("{world}", world.name))
            worlds.blacklistedSound.play(audience)
            return true
        }

        // Player's world is not whitelisted
        if (!worlds.isWhitelisted(world, placeholders, values)) {
            worlds.notWhitelistedMessage.send(audience, args.add("{world}", world.name))
            worlds.notWhitelistedSound.play(audience)
            return true
        }

        return false
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
            val receiverUuid: UUID? = compound.getReceiverUUID()

            // If the receiver name is null it means that this voucher was crated before v2.0.0
            if (receiverUuid != null && receiverUuid != this.player.uniqueId) {
                bindToReceiver.message.send(audience, args.add("{player}", Bukkit.getOfflinePlayer(receiverUuid).name ?: ""))
                bindToReceiver.sound.play(audience)
                return
            }
        }

        if (hasReachedLimit(player, voucher, args)) {
            return
        }

        if (isOnCooldown(player, voucher, args)) {
            return
        }

        if (isNotAllowedInWorld(player, voucher, args)) {
            return
        }

        if (compatibilityHandler.hasWorldGuardSupport) {
            val regions = settings.regions
            val worldGuardRegions = compatibilityHandler.worldGuardCompatibility.getRegions(player.location)

            // The player is inside a blacklisted region
            if (regions.isBlacklisted(worldGuardRegions, placeholders, values)) {
                regions.blacklistedMessage.send(audience, args)
                regions.blacklistedSound.play(audience)
                return
            }

            // The player is not inside a whitelisted region
            if (!regions.isWhitelisted(worldGuardRegions, placeholders, values)) {
                regions.notWhitelistedMessage.send(audience, args)
                regions.notWhitelistedSound.play(audience)
                return
            }
        }

        val permissions = settings.permissions

        // The player is blacklisted by permission
        if (permissions.isBlacklisted(this.player, placeholders, values)) {
            permissions.blacklistedMessage.send(audience, args)
            permissions.blacklistedSound.play(audience)
            return
        }

        // The player is not whitelisted
        if (!permissions.isWhitelisted(this.player, placeholders, values)) {
            permissions.notWhitelistedMessage.send(audience, args)
            permissions.notWhitelistedSound.play(audience)
            return
        }

        if (!settings.requirementsList.check(player, Arguments.of(emptyMap()))) {
            return
        }

        val isBulk = settings.bulkOpen.enabled && player.isSneaking

        if (settings.confirmationEnabled) {
            voucherManager.openConfirmation(player, voucher, item, args, isBulk)
        } else {
            voucher.redeem(player, item, args, plugin, isBulk)
        }
    }

}