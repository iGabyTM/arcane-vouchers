package me.gabytm.minecraft.arcanevouchers.actions.implementations.command

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.UsageBuilder
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player

class PlayerCommandAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    override fun getName(): String = "Player"

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            player.chat('/' + meta.getParsedData(player, context))
        }
    }

    companion object {

        private val USAGE = UsageBuilder("player")
            .hover(text("Execute a command as the player that is using the voucher"))
            // Arguments
            .required(
                true,
                "command",
                UsageBuilder.STRING,
                "the command to execute (the slash is not required, unless the command has two or more)"
            )
            .build()

        @Suppress("unused")
        @JvmStatic
        private fun usage(): Component = USAGE

    }

}