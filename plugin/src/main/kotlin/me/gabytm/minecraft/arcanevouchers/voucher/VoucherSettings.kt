package me.gabytm.minecraft.arcanevouchers.voucher

import me.gabytm.minecraft.arcanevouchers.functions.parseTime
import me.gabytm.minecraft.arcanevouchers.functions.replace
import me.gabytm.minecraft.arcanevouchers.limit.LimitType
import me.gabytm.minecraft.arcanevouchers.message.Message
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import kotlin.math.max

class VoucherSettings(
    val bulkOpen: BulkOpen = BulkOpen(),
    val messages: Messages = Messages(),
    val confirmationEnabled: Boolean = false,
    val limit: Limit = Limit(),
    val cooldown: Cooldown = Cooldown(),
    val permissions: Permissions = Permissions(),
    val worlds: Worlds = Worlds(),
    val regions: Regions = Regions(),
    val bindToReceiver: BindToReceiver = BindToReceiver()
) {

    data class BulkOpen(
        val enabled: Boolean = false,
        val limit: Int = Int.MAX_VALUE
    )

    data class Messages(
        val receiveMessage: Message = Message.NONE,
        val redeemMessage: Message = Message.NONE
    )

    data class Limit(
        val enabled: Boolean = false,
        val type: LimitType = LimitType.NONE,
        val limit: Long = 0L,
        val message: Message = Message.NONE
    )

    data class Cooldown(
        val enabled: Boolean = false,
        val cooldown: Long = 0L,
        val message: Message = Message.NONE
    )

    class Permissions(
        private val whitelistPermissions: List<String> = emptyList(),
        val notWhitelistedMessage: Message = Message.NONE,
        private val blacklistPermissions: List<String> = emptyList(),
        val blacklistedMessage: Message = Message.NONE
    ) {

        fun isWhitelisted(player: Player, placeholders: Array<String>, values: Array<String>): Boolean {
            if (this.whitelistPermissions.isEmpty()) {
                return true
            }

            return this.whitelistPermissions.any { player.hasPermission(it.replace(placeholders, values)) }
        }

        fun isBlacklisted(player: Player, placeholders: Array<String>, values: Array<String>): Boolean {
            if (this.blacklistPermissions.isEmpty()) {
                return false
            }

            return this.blacklistPermissions.any { player.hasPermission(it.replace(placeholders, values)) }
        }

    }

    class Worlds(
        private val whitelistedWorlds: OptionHolder = OptionHolder(),
        val notWhitelistedMessage: Message = Message.NONE,
        private val blacklistWorlds: OptionHolder = OptionHolder(),
        val blacklistedMessage: Message = Message.NONE
    ) {

        fun isWhitelisted(world: World, placeholders: Array<String>, values: Array<String>): Boolean {
            if (this.whitelistedWorlds.isEmpty()) {
                return true
            }

            return this.whitelistedWorlds.any(world.name, placeholders, values)
        }

        fun isBlacklisted(world: World, placeholders: Array<String>, values: Array<String>): Boolean {
            if (this.blacklistWorlds.isEmpty()) {
                return false
            }

            return this.blacklistWorlds.any(world.name, placeholders, values)
        }

    }

    class Regions(
        private val whitelist: OptionHolder = OptionHolder(),
        val notWhitelistedMessage: Message = Message.NONE,
        private val blacklist: OptionHolder = OptionHolder(),
        val blacklistedMessage: Message = Message.NONE
    ) {

        fun isWhitelisted(regions: Set<String>, placeholders: Array<String>, values: Array<String>): Boolean {
            if (this.whitelist.isEmpty()) {
                return true
            }

            return this.whitelist.any(regions, placeholders, values)
        }

        fun isBlacklisted(regions: Set<String>, placeholders: Array<String>, values: Array<String>): Boolean {
            if (this.blacklist.isEmpty()) {
                return false
            }

            return this.blacklist.any(regions, placeholders, values)
        }

    }

    data class BindToReceiver(
        val enabled: Boolean = false,
        val message: Message = Message.NONE
    )

    companion object {

        fun from(config: ConfigurationSection?): VoucherSettings {
            if (config == null) {
                return VoucherSettings()
            }

            val bulkOpen = BulkOpen(
                config.getBoolean("bulkOpen.enabled"),
                max(1, config.getInt("bulkOpen.limit"))
            )

            val messages = Messages(
                Message.create(config.getString("messages.receive") ?: ""),
                Message.create(config.getString("messages.redeem") ?: "")
            )

            val confirmationEnabled = config.getBoolean("confirmation.enabled")

            val limit = Limit(
                config.getBoolean("limit.enabled"),
                LimitType.getLimit(config.getString("limit.type") ?: ""),
                config.getLong("limit.limit"),
                Message.create(config.getString("limit.message") ?: "")
            )

            val cooldown = Cooldown(
                config.getBoolean("cooldown.allowBulkOpen", true),
                (config.getString("cooldown.cooldown") ?: "").parseTime(TimeUnit.MILLISECONDS),
                Message.create(config.getString("cooldown.message") ?: "")
            )

            val permissions = Permissions(
                config.getStringList("permissions.whitelist.list"),
                Message.create(config.getString("permissions.whitelist.message") ?: ""),
                config.getStringList("permissions.blacklist.list"),
                Message.create(config.getString("permissions.blacklist.message") ?: "")
            )

            val worlds = Worlds(
                OptionHolder.from(config.getStringList("worlds.whitelist.list")),
                Message.create(config.getString("worlds.whitelist.message") ?: ""),
                OptionHolder.from(config.getStringList("worlds.blacklist.list")),
                Message.create(config.getString("worlds.blacklist.message") ?: "")
            )

            val regions = Regions(
                OptionHolder.from(config.getStringList("regions.whitelist.list")),
                Message.create(config.getString("regions.whitelist.message") ?: ""),
                OptionHolder.from(config.getStringList("regions.blacklist.list")),
                Message.create(config.getString("regions.blacklist.message") ?: "")
            )

            val bindToReceiver = BindToReceiver(
                config.getBoolean("bindToReceiver.enabled"),
                Message.create(config.getString("bindToReceiver.message") ?: "")
            )

            return VoucherSettings(bulkOpen, messages, confirmationEnabled, limit, cooldown, permissions, worlds, regions, bindToReceiver)
        }

    }

}