package me.gabytm.minecraft.arcanevouchers.actions

import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionHandler
import me.gabytm.minecraft.arcanevouchers.actions.permission.PermissionMode
import me.gabytm.util.actions.actions.Action
import me.gabytm.util.actions.actions.ActionMeta
import org.bukkit.entity.Player

abstract class ArcaneAction(meta: ActionMeta<Player>, private val handler: PermissionHandler) : Action<Player>(meta) {

    private val defaultName = javaClass.simpleName.replace("Action", "")

    private val permissionMode: PermissionMode
    private val permission: String

    init {
        val perm = meta.properties["permission"]

        if (perm == null) {
            this.permissionMode = PermissionMode.NONE
            this.permission = ""
        } else {
            val (permission, mode) = PermissionMode.getPermissionAndMode(perm)
            this.permissionMode = mode
            this.permission = permission
        }
    }

    /**
     * Get the name of the action, currently used for bStats
     * @return the name of the action
     */
    open fun getName(): String = defaultName

    protected fun execute(player: Player, action: () -> Unit) {
        permissionMode.execute(handler, player, permission, action)
    }

}