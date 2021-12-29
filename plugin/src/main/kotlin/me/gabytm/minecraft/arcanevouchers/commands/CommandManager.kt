package me.gabytm.minecraft.arcanevouchers.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.message.Lang
import me.mattstudios.mf.base.CommandManager
import me.mattstudios.mf.base.MessageHandler

class CommandManager(plugin: ArcaneVouchers) {

    init {
        val manager = CommandManager(plugin, true)

        registerMessages(manager.messageHandler)

        manager.register(
            GiveCommand(plugin, plugin.voucherManager),
            ReloadCommand(plugin)
        )
    }

    private fun registerMessages(handler: MessageHandler) {
        mutableMapOf(
            "cmd.no.permission" to Lang.GENERAL__NO_PERMISSION
        ).forEach { (key, message) -> handler.register(key, message::send) }
    }

}