package me.gabytm.minecraft.arcanevouchers.actions.permission

import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PermissionHandler {

    var hasPermissionSupport: Boolean = false
        private set
    lateinit var permission: Permission
        private set

    init {
        setup()
    }

    private fun setup() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return
        }

        val rsp = Bukkit.getServer().servicesManager.getRegistration(Permission::class.java) ?: return

        this.permission = rsp.provider
        this.hasPermissionSupport = true
    }

    fun add(player: Player, permission: String) {
        this.permission.playerAdd(null, player, permission)
    }

    fun remove(player: Player, permission: String) {
        this.permission.playerRemove(null, player, permission)
    }

}