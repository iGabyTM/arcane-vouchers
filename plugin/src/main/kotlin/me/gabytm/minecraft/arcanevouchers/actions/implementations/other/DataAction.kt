package me.gabytm.minecraft.arcanevouchers.actions.implementations.other

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import me.gabytm.util.actions.actions.implementations.DataAction
import org.bukkit.entity.Player

class DataAction(meta: ActionMeta<Player>, handler: PermissionHandler) : ArcaneAction(meta, handler) {

    private val base = DataAction(meta)

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            base.run(player, context)
        }
    }

}