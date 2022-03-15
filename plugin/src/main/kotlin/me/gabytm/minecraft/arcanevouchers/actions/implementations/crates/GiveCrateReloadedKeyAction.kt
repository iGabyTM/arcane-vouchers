package me.gabytm.minecraft.arcanevouchers.actions.implementations.crates

import com.hazebyte.crate.api.CrateAPI
import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.warning
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import org.bukkit.entity.Player

class GiveCrateReloadedKeyAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            // Format: <crate> (amount: 1)
            val parts = meta.getParsedData(player, context).split(Constant.Separator.SPACE, 2)

            val amount = if (parts.size == 1) 1 else parts[1].toIntOrNull() ?: kotlin.run {
                warning("Could not parse integer from '${parts[1]}', ($ID, ${meta.rawData})")
                return@execute
            }
            val crate = CrateAPI.getCrateRegistrar().getCrate(parts[0]) ?: kotlin.run {
                warning("Unknown crate '${parts[0]}' ($ID, ${meta.rawData})")
                return@execute
            }

            crate.giveTo(player, amount)
        }
    }

    companion object {
        const val ID: String = "givecratereloadedkey"
    }

}