package me.gabytm.minecraft.arcanevouchers.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.voucher.VoucherManager
import me.mattstudios.mf.annotations.CompleteFor
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

class GiveCommand(val plugin: ArcaneVouchers, private val manager: VoucherManager) : ArcaneCommand(plugin) {

    @Permission(Constant.Permission.ADMIN)
    @SubCommand("give")
    fun onCommand(sender: CommandSender, input: Array<String>) {
        // Even though only the first 2 arguments are required, 'give' is also in the array
        if (input.size < 3) {
            sender.sendMessage("/av give [target] [voucher] (amount) (args...)")
            return
        }

        // Get rid of the 'give' argument
        val args = input.copyOfRange(1, input.size)

        val receiver = Bukkit.getPlayer(args[RECEIVER]) ?: kotlin.run {
            sender.sendMessage("Unknown player ${args[RECEIVER]}")
            return
        }

        val voucher = this.manager.getVoucher(args[VOUCHER]) ?: kotlin.run {
            sender.sendMessage("Unknown voucher ${args[VOUCHER]}")
            return
        }

        val amount = (if (args.size > 2) args[AMOUNT].toIntOrNull() else 1) ?: kotlin.run {
            sender.sendMessage("Invalid amount ${args[AMOUNT]}")
            return
        }

        val arguments = if (args.size > 3) args.copyOfRange(ARGS, args.size) else arrayOf()
        this.manager.giveVoucher(receiver, voucher, amount, arguments)
    }

    @CompleteFor("give")
    fun tabCompletion(args: List<String>, player: Player): MutableList<String> {
        if (!player.hasPermission(Constant.Permission.ADMIN)) {
            return mutableListOf()
        }

        return when (args.size - 1) {
            // /av give [receiver]
            RECEIVER -> StringUtil.copyPartialMatches(args[RECEIVER], Bukkit.getOnlinePlayers().map { it.name }, mutableListOf())
            // /av give [receiver] [voucher]
            VOUCHER -> StringUtil.copyPartialMatches(args[VOUCHER], manager.getVoucherIds(), mutableListOf())
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
        private const val ARGS = 3 // 3+

    }

}