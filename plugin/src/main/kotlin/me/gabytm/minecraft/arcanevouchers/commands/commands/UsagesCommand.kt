package me.gabytm.minecraft.arcanevouchers.commands.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.commands.ArcaneCommand
import me.gabytm.minecraft.arcanevouchers.functions.name
import me.gabytm.minecraft.arcanevouchers.limit.LimitManager
import me.gabytm.minecraft.arcanevouchers.limit.LimitType
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.gabytm.minecraft.arcanevouchers.voucher.Voucher
import me.mattstudios.mf.annotations.CompleteFor
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import me.mattstudios.mf.annotations.WrongUsage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class UsagesCommand(plugin: ArcaneVouchers) : ArcaneCommand(plugin) {

    private val loadedActions = mutableMapOf(
        "check" to CheckAction(this, plugin.voucherManager.limitManager),
        "modify" to ModifyAction(this, plugin.voucherManager.limitManager),
        "set" to SetAction(this, plugin.voucherManager.limitManager)
    )

    @WrongUsage("command.usages.usage")
    @Permission(Constant.Permission.ADMIN)
    @SubCommand("usages")
    fun onCommand(sender: CommandSender, input: Array<String>) {
        // Even though only the first 2 arguments are required, 'limit' is also in the array
        if (input.size < 3) {
            Lang.USAGES__USAGE.send(sender)
            return
        }

        // Get rid of the 'limit' argument
        val args = input.copyOfRange(1, input.size)

        val voucher = plugin.voucherManager.getVoucher(args[0]) ?: kotlin.run {
            Lang.GENERAL__UNKNOWN__VOUCHER.send(sender, args[0])
            return
        }

        // Let the sender know that the limit of this voucher is NONE
        if (voucher.settings.limit.type == LimitType.NONE) {
            Lang.USAGES__TYPE_NONE.send(sender, voucher.id)
            return
        }

        // Find an action by the given name and then call 'execute(...)'
        this.loadedActions[args[1].lowercase()]?.execute(sender, voucher, args.copyOfRange(2, args.size))
    }

    @CompleteFor("usages")
    fun tabCompletion(args: List<String>, player: Player): List<String> {
        return when (args.size) {
            // /av usages [voucher]
            1 -> createVoucherCompletion(args[0])
            // /av usages [voucher] [action]
            2 -> StringUtil.copyPartialMatches(args[1], this.loadedActions.keys, mutableListOf())
            // /av usages [voucher] [action] (other arguments)
            else -> {
                val voucher = this.plugin.voucherManager.getVoucher(args[0]) ?: return mutableListOf()
                val action = this.loadedActions[args[1].lowercase()] ?: return mutableListOf()

                action.completion(player, voucher, args.subList(2, args.size))
            }
        }
    }

    private abstract class LimitAction(protected val command: ArcaneCommand, protected val manager: LimitManager) {

        abstract fun execute(sender: CommandSender, voucher: Voucher, args: Array<String>)

        abstract fun completion(player: Player, voucher: Voucher, args: List<String>): List<String>

    }

    /**
     * Usage: /av usages `[voucher]` check (player, if limit.type is [LimitType.PERSONAL])
     */
    private class CheckAction(command: ArcaneCommand, manager: LimitManager) : LimitAction(command, manager) {

        override fun execute(sender: CommandSender, voucher: Voucher, args: Array<String>) {
            val limit = voucher.settings.limit

            // Send the message for global limit
            if (limit.type == LimitType.GLOBAL) {
                Lang.USAGES__CHECK__GLOBAL.send(sender, limit.limit, manager.getGlobalUsages(voucher), voucher.id)
                return
            }

            // The action require a player name if the limit.type is PERSONAL
            if (args.isEmpty()) {
                Lang.USAGES__CHECK__PERSONAL__REQUIRE_PLAYER.send(sender)
                return
            }

            val player = Bukkit.getOfflinePlayer(args[0])

            // The player was not found
            if (!player.hasPlayedBefore()) {
                Lang.GENERAL__UNKNOWN__PLAYER.send(sender, args[0])
                return
            }

            Lang.USAGES__CHECK__PERSONAL.send(
                sender,
                limit.limit, player.name(args[0]), manager.getPersonalUsages(player.uniqueId, voucher), voucher.id
            )
        }

        override fun completion(player: Player, voucher: Voucher, args: List<String>): List<String> {
            // /av usages [voucher] check (player)
            if (args.size == 1 && voucher.settings.limit.type == LimitType.PERSONAL) {
                return command.createPlayersCompletion(args[0])
            }

            return mutableListOf()
        }

    }

    /**
     * Usage: /av usages `[voucher]` modify `[value]` (player, if limit.type is [LimitType.PERSONAL])
     */
    private class ModifyAction(command: ArcaneCommand, manager: LimitManager) : LimitAction(command, manager) {

        override fun execute(sender: CommandSender, voucher: Voucher, args: Array<String>) {
            // The action require a 'value' argument
            if (args.isEmpty()) {
                Lang.USAGES__MODIFY__USAGE.send(sender)
                return
            }

            // Couldn't parse the string to a long
            val value = args[0].toLongOrNull() ?: kotlin.run {
                Lang.GENERAL__INVALID__NUMBER__LONG.send(sender, args[0])
                return
            }

            // The limit.type is GLOBAL
            if (voucher.settings.limit.type == LimitType.GLOBAL) {
                val newLimit = manager.modifyGlobalUsages(voucher.id, value, false)
                Lang.USAGES__MODIFY__GLOBAL__CONFIRMATION.send(sender, newLimit, (if (value > 0) "+$value" else value), voucher.id)
                return
            }

            // If the limit.type is PERSONAL, the action require a player name
            if (args.size < 2) {
                Lang.USAGES__MODIFY__PERSONAL__REQUIRE_PLAYER.send(sender)
                return
            }

            val player = Bukkit.getOfflinePlayer(args[1])

            // The player was not found
            if (!player.hasPlayedBefore()) {
                Lang.GENERAL__UNKNOWN__PLAYER.send(sender, args[1])
                return
            }

            val newLimit = this.manager.modifyPersonalUsages(player.uniqueId, voucher.id, value, false)
            Lang.USAGES__MODIFY__PERSONAL__CONFIRMATION.send(
                sender,
                newLimit, player.name(args[1]), (if (value > 0) "+$value" else value), voucher.id
            )
        }

        override fun completion(player: Player, voucher: Voucher, args: List<String>): List<String> {
            // /av usages [voucher] modify [value] (player)
            if (args.size == 2 && voucher.settings.limit.type == LimitType.PERSONAL) {
                return command.createPlayersCompletion(args[1])
            }

            return mutableListOf()
        }

    }

    /**
     * Usage: /av usages `[voucher]` set `[newValue]` (player, if limit.type is [LimitType.PERSONAL])
     */
    private class SetAction(command: ArcaneCommand, manager: LimitManager) : LimitAction(command, manager) {

        override fun execute(sender: CommandSender, voucher: Voucher, args: Array<String>) {
            // The action require a 'newValue'
            if (args.isEmpty()) {
                Lang.USAGES__SET__USAGE.send(sender)
                return
            }

            // Could not parse the argument to a long
            val newValue = args[0].toLongOrNull() ?: kotlin.run {
                Lang.GENERAL__INVALID__NUMBER__LONG.send(sender, args[0])
                return
            }

            if (voucher.settings.limit.type == LimitType.GLOBAL) {
                manager.modifyGlobalUsages(voucher.id, newValue, true)
                Lang.USAGES__SET__GLOBAL__CONFIRMATION.send(sender, newValue, voucher.id)
                return
            }

            if (args.size < 2) {
                Lang.USAGES__SET__PERSONAL__REQUIRE_PLAYER.send(sender)
                return
            }

            val player = Bukkit.getOfflinePlayer(args[1])

            if (!player.hasPlayedBefore()) {
                Lang.GENERAL__UNKNOWN__PLAYER.send(sender, args[1])
                return
            }

            this.manager.modifyPersonalUsages(player.uniqueId, voucher.id, newValue, true)
            Lang.USAGES__SET__PERSONAL__CONFIRMATION.send(sender, newValue, player.name(args[1]), voucher.id)
        }

        override fun completion(player: Player, voucher: Voucher, args: List<String>): List<String> {
            // /av usages [voucher] set [newValue] (player)
            if (args.size == 2 && voucher.settings.limit.type == LimitType.PERSONAL) {
                return command.createPlayersCompletion(args[1])
            }

            return mutableListOf()
        }

    }

}