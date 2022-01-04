package me.gabytm.minecraft.arcanevouchers.cooldown

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.Permission
import me.gabytm.minecraft.arcanevouchers.functions.any
import me.gabytm.minecraft.arcanevouchers.voucher.Voucher
import org.bukkit.entity.Player
import java.util.*

class CooldownManager(private val plugin: ArcaneVouchers) {

    private val activeCooldowns: Table<UUID, String, Long> = HashBasedTable.create()
    private val storage = CooldownStorageHandler(plugin)

    init {
        this.activeCooldowns.putAll(this.storage.loadCooldowns())
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

}