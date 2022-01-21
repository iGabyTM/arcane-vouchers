package me.gabytm.minecraft.arcanevouchers.actions.implementations.economy

import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.functions.giveItems
import me.gabytm.minecraft.arcanevouchers.items.ItemCreator
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import org.bukkit.entity.Player

class ItemAction(meta: ActionMeta<Player>, handler: PermissionHandler, private val itemCreator: ItemCreator) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            player.giveItems(itemCreator.create(meta.getParsedData(player, context)) ?: return@execute)
        }
    }

}