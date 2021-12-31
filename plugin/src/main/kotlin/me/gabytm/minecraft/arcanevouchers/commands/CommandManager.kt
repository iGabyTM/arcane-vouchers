package me.gabytm.minecraft.arcanevouchers.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.commands.commands.GiveCommand
import me.gabytm.minecraft.arcanevouchers.commands.commands.LimitCommand
import me.gabytm.minecraft.arcanevouchers.commands.commands.ReloadCommand
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.mattstudios.mf.base.CommandManager
import me.mattstudios.mf.base.MessageHandler

class CommandManager(plugin: ArcaneVouchers) {

    init {
        val manager = CommandManager(plugin, true)

        registerMessages(manager.messageHandler)

        manager.register(
            GiveCommand(plugin),
            LimitCommand(plugin),
            ReloadCommand(plugin)
        )
    }

    private fun registerMessages(handler: MessageHandler) {
        mutableMapOf(
            // Default messages
            "cmd.no.permission" to Lang.GENERAL__NO_PERMISSION,
            // Plugin messages
            "command.give.usage" to Lang.GIVE__USAGE,
            "command.limit.usage" to Lang.LIMIT__USAGE
        ).forEach { (key, message) -> handler.register(key, message::send) }
    }

}