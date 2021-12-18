package me.gabytm.minecraft.arcanevouchers.actions.implementations.other

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.milkbowl.vault.economy.Economy
import org.bukkit.entity.Player

class AddMoneyAction(
    meta: ActionMeta<Player>,
    handler: PermissionHandler,
    private val economy: Economy
) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            val amount = meta.getParsedData(player, context).toDoubleOrNull() ?: return@execute
            economy.depositPlayer(player, amount)
        }
    }

}