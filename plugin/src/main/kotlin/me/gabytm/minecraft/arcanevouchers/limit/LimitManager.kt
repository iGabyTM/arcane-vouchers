package me.gabytm.minecraft.arcanevouchers.limit

import com.google.common.collect.HashBasedTable
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant.Permission
import me.gabytm.minecraft.arcanevouchers.functions.any
import me.gabytm.minecraft.arcanevouchers.functions.async
import me.gabytm.minecraft.arcanevouchers.functions.info
import me.gabytm.minecraft.arcanevouchers.voucher.Voucher
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class LimitManager(plugin: ArcaneVouchers) {

    private val globalUsages = mutableMapOf<String, Long>()
    private val personalUsages = HashBasedTable.create<UUID, String, Long>()

    private val limitUpdateFunction: (Long, Long) -> Long = { a, b -> a + b }

    private val storage = UsagesStorageHandler(plugin)

    init {
        this.globalUsages.putAll(this.storage.loadGlobalUsages())
        this.personalUsages.putAll(this.storage.loadPersonalUsages())
    }

    /**
     * Check if a [Player] bypass the [limit][me.gabytm.minecraft.arcanevouchers.voucher.VoucherSettings.Limit] of a [Voucher]
     * @param player to check
     * @param voucher to check
     * @return whether the [Player] bypass the limit
     */
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

    /**
     * Retrieve how many times a [Player] has used a [Voucher]
     * @param player the [UUID] of a player (online or offline)
     * @param voucher voucher
     * @return usages or `0` if limit [type][me.gabytm.minecraft.arcanevouchers.voucher.VoucherSettings.Limit.type] is [NONE][LimitType.NONE]
     */
    fun getUsages(player: UUID, voucher: Voucher): Long {
        return when (voucher.settings.limit.type) {
            LimitType.GLOBAL -> this.getGlobalUsages(voucher)
            LimitType.PERSONAL -> this.getPersonalUsages(player, voucher)
            LimitType.NONE -> 0L
        }
    }

    /**
     * Retrieve the global usages of a [Voucher]
     * @param voucher the [id][Voucher.id] of a voucher (case-sensitive)
     * @return global usages of the voucher
     */
    fun getGlobalUsages(voucher: String): Long = this.globalUsages[voucher] ?: 0L

    /**
     * Retrieve the global usages of a [Voucher]
     * @param voucher voucher
     * @return global usages of the voucher
     */
    fun getGlobalUsages(voucher: Voucher): Long = this.getGlobalUsages(voucher.id)

    /**
     * Retrieve how many times a [Player] has used a [Voucher]
     * @param player the [UUID] of a player (online or offline)
     * @param voucher the [id][Voucher.id] of a voucher (case-sensitive)
     * @return usages or `0` if they haven't yet used this type of voucher
     */
    fun getPersonalUsages(player: UUID, voucher: String): Long = this.personalUsages[player, voucher] ?: 0L

    /**
     * Retrieve how many times a [Player] has used a [Voucher]
     * @param player the [UUID] of a player (online or offline)
     * @param voucher
     * @return usages or `0` if they haven't yet used this type of voucher
     */
    fun getPersonalUsages(player: UUID, voucher: Voucher): Long = this.getPersonalUsages(player, voucher.id)

    /**
     * Increase the usages of a [Player] for a certain [Voucher] by `value`
     * @param player the [UUID] of a player (online or offline)
     * @param voucher the [id][Voucher.id] of a voucher (case-sensitive)
     * @param value the value that will be added to the total usages
     */
    fun increaseUsages(player: UUID, voucher: String, value: Long) {
        this.globalUsages.merge(voucher, value, limitUpdateFunction)?.let {
            this.storage.updateGlobalUsages(voucher, it)
        }
        this.personalUsages.row(player).merge(voucher, value, limitUpdateFunction)?.let {
            this.storage.updatePersonalUsages(player, voucher, it)
        }
    }

    /**
     * Modify the global usages of a [Voucher] by `value`.
     * If `set` is `false`, a positive `value` will be added to the total usages and a negative `value` will be subtracted
     * @param voucher the [id][Voucher.id] of a voucher (case-sensitive)
     * @param value the value that will be added / subtracted from the total usages
     * @param set if true, the `value` will be set as total usages
     * @return the new usages or `0` if `value == 0L && !set`
     */
    fun modifyGlobalUsages(voucher: String, value: Long, set: Boolean = false): Long {
        // When the value is 0 and 'set' is false, meaning we have to add/subtract the value, just return to avoid unnecessary operations
        if (value == 0L && !set) {
            return 0L
        }

        return if (set) {
            this.globalUsages[voucher] = value
            this.storage.updateGlobalUsages(voucher, value)
            value
        } else {
            val newLimit = this.globalUsages.merge(voucher, value, limitUpdateFunction) ?: return -1
            this.storage.updateGlobalUsages(voucher, newLimit)
            newLimit
        }
    }

    /**
     * Modify the personal usages of a [Player] for a [Voucher] by `value`.
     * If `set` is `false`, a positive `value` will be added to the total usages and a negative `value` will be subtracted
     * @param player the [UUID] of a player (online or offline)
     * @param voucher the [id][Voucher.id] of a voucher (case-sensitive)
     * @param value the value that will be added / subtracted from the total usages
     * @param set if true, the `value` will be set as total usages
     * @return the new usages or `0` if `value == 0L && !set`
     */
    fun modifyPersonalUsages(player: UUID, voucher: String, value: Long, set: Boolean = false): Long {
        // When the value is 0 and 'set' is false, meaning we have to add/subtract the value, just return to avoid unnecessary operations
        if (value == 0L && !set) {
            return 0L
        }

        return if (set) {
            this.personalUsages.put(player, voucher, value)
            this.storage.updatePersonalUsages(player, voucher, value)
            value
        } else {
            val newLimit = this.personalUsages.row(player).merge(voucher, value, limitUpdateFunction) ?: return -1
            this.storage.updatePersonalUsages(player, voucher, newLimit)
            newLimit
        }
    }

    /**
     * Do a bulk modification for personal usages of a [Voucher], similar with calling [modifyPersonalUsages] for each
     * player, one by one. The operation is done [async]
     * @param onlyOnlinePlayers if true, it will affect only the [online players][Bukkit.getOnlinePlayers], otherwise will affect [everyone][Bukkit.getOfflinePlayers]
     * @param voucher the [id][Voucher.id] of a voucher (case-sensitive)
     * @param value the value that will be added / subtracted from the total usages
     * @param set if true, the `value` will be set as total usages
     * @return the new usages or `0` if `value == 0L && !set`
     */
    fun modifyPersonalUsages(onlyOnlinePlayers: Boolean, voucher: String, value: Long, set: Boolean = false) {
        // When the value is 0 and 'set' is false, meaning we have to add/subtract the value, just return to avoid unnecessary operations
        if (value == 0L && !set) {
            return
        }

        async {
            val players = if (onlyOnlinePlayers) {
                Bukkit.getOnlinePlayers().map { it.uniqueId }
            } else {
                Bukkit.getOfflinePlayers().map { it.uniqueId }
            }

            if (onlyOnlinePlayers) {
                info("Preparing to modify the personal limit of all ${players.size} online players for voucher $voucher. (value: $value, set: $set)")
            } else {
                info("Preparing to modify the personal limit of all players (${players.size}) for voucher $voucher. (value: $value, set: $set)")
            }

            for (player in players) {
                modifyPersonalUsages(player, voucher, value, set)
            }
        }
    }

}