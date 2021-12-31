package me.gabytm.minecraft.arcanevouchers.commands.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.commands.ArcaneCommand
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.mattstudios.mf.annotations.CompleteFor
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import me.mattstudios.mf.annotations.WrongUsage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GiveCommand(plugin: ArcaneVouchers) : ArcaneCommand(plugin) {

    @WrongUsage("command.give.usage")
    @Permission(Constant.Permission.ADMIN)
    @SubCommand("give")
    fun onCommand(sender: CommandSender, input: Array<String>) {
        // Even though only the first 2 arguments are required, 'give' is also in the array
        if (input.size < 3) {
            Lang.GIVE__USAGE.send(sender)
            return
        }

        // Get rid of the 'give' argument
        val args = input.copyOfRange(1, input.size)

        val giveToEveryone = args[RECEIVER] == ALL_ONLINE_PLAYERS
        val receiver = if (giveToEveryone) null else Bukkit.getPlayer(args[RECEIVER])

        // The player wasn't found
        if (receiver == null && !giveToEveryone) {
            Lang.GENERAL__UNKNOWN__PLAYER.send(sender, args[RECEIVER])
            return
        }

        val voucher = this.plugin.voucherManager.getVoucher(args[VOUCHER]) ?: kotlin.run {
            Lang.GENERAL__UNKNOWN__VOUCHER.send(sender, args[VOUCHER])
            return
        }

        val amount = (if (args.size > 2) args[AMOUNT].toIntOrNull() else 1) ?: kotlin.run {
            Lang.GENERAL__INVALID__NUMBER__INTEGER.send(sender, args[AMOUNT])
            return
        }

        val arguments = if (args.size > 3) args.copyOfRange(ARGS_START_INDEX, args.size) else arrayOf()

        if (giveToEveryone) {
            Bukkit.getOnlinePlayers().map { this.plugin.voucherManager.giveVoucher(it, voucher, amount, arguments) }
            return
        }

        // Just to avoid 'receiver!!', at this point receiver shouldn't be null anyway
        if (receiver != null) {
            this.plugin.voucherManager.giveVoucher(receiver, voucher, amount, arguments)
            Lang.GIVE__SENDER.send(sender, listOf(amount, receiver.name, voucher.id))
        }
    }

    @CompleteFor("give")
    fun tabCompletion(args: List<String>, player: Player): MutableList<String> {
        return when (args.size - 1) {
            // /av give [receiver]
            RECEIVER -> createPlayersCompletion(args[RECEIVER], true)
            // /av give [receiver] [voucher]
            VOUCHER -> createVoucherCompletion(args[VOUCHER])
            else -> mutableListOf()
        }
    }

    companion object {

        /**
         * The index of each command argument
         */
        private const val RECEIVER = 0
        private const val VOUCHER = 1
        private const val AMOUNT = 2
        private const val ARGS_START_INDEX = 3

    }

}