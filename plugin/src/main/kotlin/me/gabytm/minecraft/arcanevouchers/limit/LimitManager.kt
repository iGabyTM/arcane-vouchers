package me.gabytm.minecraft.arcanevouchers.limit

import com.google.common.collect.HashBasedTable
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.Permission
import me.gabytm.minecraft.arcanevouchers.functions.any
import me.gabytm.minecraft.arcanevouchers.voucher.Voucher
import org.bukkit.entity.Player
import java.util.*

class LimitManager(plugin: ArcaneVouchers) {

    private val globalLimits = mutableMapOf<String, Long>()
    private val personalLimits = HashBasedTable.create<UUID, String, Long>()

    private val limitIncreaseFunction: (Long, Long) -> Long? = { a, b -> a + b }

    private val storage = LimitStorageHandler(plugin)

    init {
        this.globalLimits.putAll(this.storage.loadGlobalLimits())
        this.personalLimits.putAll(this.storage.loadPersonalLimits())
    }

    fun bypassLimit(player: Player, voucher: Voucher): Boolean {
        return when (voucher.settings.limit.type) {
            LimitType.GLOBAL -> player.any(
                Permission.LIMIT_BYPASS,
                Permission.LIMIT_BYPASS_ALL_GLOBAL,
                Permission.LIMIT_BYPASS_GLOBAL.format(voucher.id)
            )
            LimitType.PERSONAL -> player.any(
                Permission.LIMIT_BYPASS,
                Permission.LIMIT_BYPASS_ALL_PERSONAL,
                Permission.LIMIT_BYPASS_PERSONAL.format(voucher.id)
            )
            LimitType.NONE -> true
        }
    }

    fun getUsages(player: UUID, voucher: Voucher): Long {
        return when (voucher.settings.limit.type) {
            LimitType.GLOBAL -> this.globalLimits[voucher.id] ?: 0L
            LimitType.PERSONAL -> this.personalLimits[player, voucher.id] ?: 0L
            LimitType.NONE -> 0L
        }
    }

    fun increaseLimit(player: UUID, voucher: String, value: Int) {
        val longValue = value.toLong()

        this.globalLimits.merge(voucher, longValue, limitIncreaseFunction)?.let {
            this.storage.updateGlobalLimit(voucher, it)
        }
        this.personalLimits.row(player).merge(voucher, longValue, limitIncreaseFunction)?.let {
            this.storage.updatePersonalLimit(player, voucher, it)
        }
    }

}