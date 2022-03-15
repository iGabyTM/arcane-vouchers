package me.gabytm.minecraft.arcanevouchers.actions.implementations.command

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.sync
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class ConsoleCommandAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    override fun getName(): String = "Console"

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            sync {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), meta.getParsedData(player, context))
            }
        }
    }

    companion object {

        private val USAGE = UsageBuilder("console")
            .hover(text("Execute command from console"))
            // Arguments
            .required(true, "command", UsageBuilder.STRING, "command to execute")
            .build()

        @Suppress("unused")
        @JvmStatic
        private fun usage(): Component = USAGE

    }

}