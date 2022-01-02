package me.gabytm.minecraft.arcanevouchers.commands.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.commands.ArcaneCommand
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import org.bukkit.command.CommandSender

class HelpCommand(plugin: ArcaneVouchers) : ArcaneCommand(plugin) {

    @Permission(Constant.Permission.ADMIN)
    @SubCommand("help")
    fun onHelpCommand(sender: CommandSender) = Lang.HELP.send(sender, plugin.description.version)

    @Permission(Constant.Permission.ADMIN)
    @Default
    fun onDefaultCommand(sender: CommandSender) = Lang.HELP.send(sender, plugin.description.version)

}