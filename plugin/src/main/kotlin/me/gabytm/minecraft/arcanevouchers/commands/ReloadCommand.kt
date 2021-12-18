package me.gabytm.minecraft.arcanevouchers.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.SubCommand
import org.bukkit.command.CommandSender

@Command("av")
class ReloadCommand(private val plugin: ArcaneVouchers) : ArcaneCommand(plugin) {

    @SubCommand("reload")
    fun onCommand(sender: CommandSender) {
        plugin.reload()
        sender.sendMessage("Reloaded!")
    }

}