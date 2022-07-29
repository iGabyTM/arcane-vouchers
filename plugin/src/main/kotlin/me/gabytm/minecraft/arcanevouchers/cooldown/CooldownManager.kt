package me.gabytm.minecraft.arcanevouchers.cooldown

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.Permission
import me.gabytm.minecraft.arcanevouchers.functions.any
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.gabytm.minecraft.arcanevouchers.voucher.Voucher
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit

class CooldownManager(private val plugin: ArcaneVouchers) {

    private val activeCooldowns: Table<UUID, String, Long> = HashBasedTable.create()
    private val storage = CooldownStorageHandler(plugin)

    init {
        this.activeCooldowns.putAll(this.storage.loadCooldowns())
    }

    private fun TextComponent.Builder.append(value: Long, singular: Lang, plural: Lang): TextComponent.Builder {
        if (value == 0L) {
            return this
        }

        val message = if (value == 1L) singular else plural
        return append(message.format(value) ?: return this)
    }

    fun addCooldown(player: UUID, voucher: String, cooldown: Long) {
        val expiration = System.currentTimeMillis() + cooldown
        activeCooldowns.put(player, voucher, expiration)

        if (cooldown >= plugin.settings.cooldownSaveThreshold) {
            this.storage.insertCooldown(player, voucher, System.currentTimeMillis() + cooldown)
        }
    }

    fun getTimeLeft(player: UUID, voucher: Voucher): Long {
        val cooldownExpirationTime = activeCooldowns.get(player, voucher.id) ?: return 0L

        if (System.currentTimeMillis() >= cooldownExpirationTime) {
            activeCooldowns.remove(player, voucher.id)
            return 0L
        }

        return cooldownExpirationTime - System.currentTimeMillis()
    }

    fun bypassCooldown(player: Player, voucher: String): Boolean {
        return player.any(Permission.COOLDOWN_BYPASS_ALL, Permission.COOLDOWN_BYPASS.format(voucher))
    }

    fun formatTimeLeft(time: Long): Component {
        if (time < 1L) {
            return Component.empty();
        }

        // If the time is less than 1 second, send 'other' message
        if (time < 1_000L) {
            return Lang.COOLDOWN__OTHER.get() ?: Component.empty()
        }

        var seconds = TimeUnit.MILLISECONDS.toSeconds(time)
        var minutes = seconds / 60
        var hours = minutes / 60
        val days = hours / 24

        seconds %= 60
        minutes %= 60
        hours %= 24

        return Component.text()
            .append(days, Lang.COOLDOWN__DAY, Lang.COOLDOWN__DAYS)
            .append(hours, Lang.COOLDOWN__HOUR, Lang.COOLDOWN__HOURS)
            .append(minutes, Lang.COOLDOWN__MINUTE, Lang.COOLDOWN__MINUTES)
            .append(seconds, Lang.COOLDOWN__SECOND, Lang.COOLDOWN__SECONDS)
            .build()
    }

}