package me.gabytm.minecraft.arcanevouchers.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.commands.commands.*
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.mattstudios.mf.base.CommandManager
import me.mattstudios.mf.base.MessageHandler

class CommandManager(plugin: ArcaneVouchers) {

    init {
        val manager = CommandManager(plugin, true)

        registerMessages(manager.messageHandler)

        manager.register(
            GiveCommand(plugin),
            HelpCommand(plugin),
            ListCommand(plugin),
            ReloadCommand(plugin),
            UsagesCommand(plugin)
        )
    }

    private fun registerMessages(handler: MessageHandler) {
        mutableMapOf(
            // Default messages
            "cmd.no.permission" to Lang.GENERAL__NO_PERMISSION,
            // Plugin messages
            "command.give.usage" to Lang.GIVE__USAGE,
            "command.usages.usage" to Lang.USAGES__USAGE
        ).forEach { (key, message) -> handler.register(key, message::send) }
    }

}