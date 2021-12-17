package me.gabytm.minecraft.arcanevouchers.commands

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.mattstudios.mf.base.CommandManager

class CommandManager(plugin: ArcaneVouchers) {

    init {
        val manager = CommandManager(plugin, true)

        manager.register(
            GiveCommand(plugin, plugin.voucherManager),
            ReloadCommand(plugin)
        )
    }

}