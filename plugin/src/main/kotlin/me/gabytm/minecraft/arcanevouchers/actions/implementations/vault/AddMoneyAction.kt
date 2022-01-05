package me.gabytm.minecraft.arcanevouchers.actions.implementations.vault

import com.google.common.primitives.Doubles
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.warning
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
            val parsedData = meta.getParsedData(player, context)
            val amount = Doubles.tryParse(parsedData) ?: kotlin.run {
                warning("Could not parse double from '$parsedData' (addmoney, '${meta.rawData}')")
                return@execute
            }
            economy.depositPlayer(player, amount)
        }
    }

}