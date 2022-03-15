package me.gabytm.minecraft.arcanevouchers.commands.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.commands.ArcaneCommand
import me.mattstudios.mf.annotations.Permission
import me.mattstudios.mf.annotations.SubCommand
import org.bukkit.entity.Player

class ActionsCommand(plugin: ArcaneVouchers) : ArcaneCommand(plugin) {

    @Permission(Constant.Permission.ADMIN)
    @SubCommand("actions")
    fun onCommand(sender: Player) = plugin.audiences.player(sender).sendMessage(plugin.actionManager.actionsMessage)

}