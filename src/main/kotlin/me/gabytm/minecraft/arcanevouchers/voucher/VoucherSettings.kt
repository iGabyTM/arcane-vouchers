package me.gabytm.minecraft.arcanevouchers.voucher

import me.gabytm.minecraft.arcanevouchers.limit.LimitType
import me.gabytm.minecraft.arcanevouchers.message.Message
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import kotlin.math.max

class VoucherSettings(
    val bulkOpen: BulkOpen = BulkOpen(),
    val messages: Messages = Messages(),
    val confirmationEnabled: Boolean = false,
    val limit: Limit = Limit(),
    val permissions: Permissions = Permissions(),
    val worlds: Worlds = Worlds(),
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
        val type: LimitType = LimitType.PERSONAL,
        val limit: Int = 0,
        val message: Message = Message.NONE
    )

    class Permissions(
        private val whitelistPermissions: List<String> = emptyList(),
        val notWhitelistedMessage: Message = Message.NONE,
        private val blacklistPermissions: List<String> = emptyList(),
        val blacklistedMessage: Message = Message.NONE
    ) {

        fun isWhitelisted(player: Player): Boolean {
            return whitelistPermissions.isEmpty() || whitelistPermissions.any { player.hasPermission(it) }
        }

        fun isBlacklisted(player: Player): Boolean {
            return blacklistPermissions.any { player.hasPermission(it) }
        }

    }

    class Worlds(
        private val whitelistedWorlds: List<String> = emptyList(),
        val notWhitelistedMessage: Message = Message.NONE,
        private val blacklistWorlds: List<String> = emptyList(),
        val blacklistedMessage: Message = Message.NONE
    ) {

        fun isWhitelisted(player: Player): Boolean {
            return whitelistedWorlds.isEmpty() || whitelistedWorlds.any { player.world.name == it }
        }

        fun isBlacklisted(player: Player): Boolean {
            return blacklistWorlds.any { player.world.name == it }
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
                config.getInt("limit.limit"),
                Message.create(config.getString("limit.message") ?: "")
            )

            val permissions = Permissions(
                config.getStringList("permissions.whitelist.list"),
                Message.create(config.getString("permissions.whitelist.message") ?: ""),
                config.getStringList("permissions.blacklist.list"),
                Message.create(config.getString("permissions.blacklist.message") ?: "")
            )

            val worlds = Worlds(
                config.getStringList("worlds.whitelist.list"),
                Message.create(config.getString("worlds.whitelist.message") ?: ""),
                config.getStringList("worlds.blacklist.list"),
                Message.create(config.getString("worlds.blacklist.message") ?: "")
            )

            val bindToReceiver = BindToReceiver(
                config.getBoolean("bindToReceiver.enabled"),
                Message.create(config.getString("bindToReceiver.message") ?: "")
            )

            return VoucherSettings(bulkOpen, messages, confirmationEnabled, limit, permissions, worlds, bindToReceiver)
        }

    }

}