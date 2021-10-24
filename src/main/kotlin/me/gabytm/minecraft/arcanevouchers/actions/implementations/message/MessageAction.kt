package me.gabytm.minecraft.arcanevouchers.actions.implementations.message

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.functions.color
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class MessageAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            val message = meta.getParsedData(player, context).color(true)
            val broadcast = meta.properties["broadcast"] ?: kotlin.run { player.sendMessage(message) }

            if (broadcast == "*") {
                Bukkit.broadcastMessage(message)
            } else {
                Bukkit.getOnlinePlayers()
                    .filter { it.world == player.world }
                    .forEach { it.sendMessage(message) }
            }

        }
    }

}