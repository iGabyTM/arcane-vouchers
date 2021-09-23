package me.gabytm.minecraft.arcanevouchers.commands

import me.gabytm.minecraft.arcanevouchers.voucher.VoucherManager
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.SubCommand
import me.mattstudios.mf.base.CommandBase
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@Command("av")
class GiveCommand(private val manager: VoucherManager) : CommandBase() {

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