package me.gabytm.minecraft.arcanevouchers.commands.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.commands.ArcaneCommand
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import org.bukkit.command.CommandSender

class ReloadCommand(plugin: ArcaneVouchers) : ArcaneCommand(plugin) {

    @Permission(Constant.Permission.ADMIN)
    @SubCommand("reload")
    fun onCommand(sender: CommandSender) {
        plugin.reload()
        Lang.RELOAD.send(sender)
    }

}