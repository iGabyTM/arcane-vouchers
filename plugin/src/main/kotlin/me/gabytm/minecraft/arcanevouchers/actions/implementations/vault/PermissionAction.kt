package me.gabytm.minecraft.arcanevouchers.actions.implementations.vault

import me.gabytm.minecraft.arcanevouchers.Constant
import me.gabytm.minecraft.arcanevouchers.actions.ArcaneAction
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.util.actions.actions.ActionMeta
import me.gabytm.util.actions.actions.Context
import net.milkbowl.vault.permission.Permission
import org.bukkit.entity.Player

class PermissionAction(
    meta: ActionMeta<Player>,
    handler: PermissionHandler,
    private val vault: Permission
) : ArcaneAction(meta, handler) {

    override fun run(player: Player, context: Context<Player>) {
        execute(player) {
            // Format: <ADD/REMOVE> <permission.node> (world)
            val parts = meta.getParsedData(player, context).split(Constant.Separator.SPACE, 3)

            if (parts.size < 2) {
                return@execute
            }

            // Check if the permission should be added or removed
            val add = parts[0].equals("ADD", true)
            val permission = parts[1]

            // The name of a world was specified
            if (parts.size == 3) {
                val world = parts[2]

                if (add) {
                    vault.playerAdd(world, player, permission)
                } else {
                    vault.playerRemove(world, player, permission)
                }

                return@execute
            }

            // Add or remove the permission globally
            if (add) {
                vault.playerAdd(null, player, permission)
            } else {
                vault.playerRemove(null, player, permission)
            }
        }
    }

}