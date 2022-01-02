package me.gabytm.minecraft.arcanevouchers.actions.implementations.economy

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import org.bukkit.entity.Player

class AddExpAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            val parsed = meta.getParsedData(player, context)

            if (parsed.endsWith('L')) {
                val levels = parsed.substring(0, parsed.length - 2).toIntOrNull() ?: kotlin.run {
                    warning("Could not parse integer from '$parsed' (addexp, ${meta.rawData})")
                    return@execute
                }
                player.giveExpLevels(levels)
            } else {
                val exp = parsed.toIntOrNull() ?: kotlin.run {
                    warning("Could not parse integer from '$parsed' (addexp, ${meta.rawData})")
                    return@execute
                }
                player.giveExp(exp)
            }
        }
    }

}