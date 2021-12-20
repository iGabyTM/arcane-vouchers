package me.gabytm.minecraft.arcanevouchers.actions.permission

import org.bukkit.entity.Player
import java.util.*

enum class PermissionMode(val prefix: String) {

    /**
     * No permission mode
     */
    NONE("") {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            action()
        }
    },

    /**
     * The permission is required for the action to run
     */
    REQUIRE("") {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            if (player.hasPermission(permission)) {
                action()
            }
        }
    },

    /**
     * The permission is required for the action to run, and it will be removed after the fist execution
     */
    REMOVE("-") {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            if (player.hasPermission(permission)) {
                handler.remove(player, permission)
                action()
            }
        }
    },

    /**
     * The action will run only if the player doesn't have the permission
     */
    NO_PERMISSION("!") {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            if (!player.hasPermission(permission)) {
                action()
            }
        }
    },

    /**
     * The permission will be added after the first execution and the action won't run next time
     */
    ADD("+") {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            if (!player.hasPermission(permission)) {
                handler.add(player, permission)
                action()
            }
        }
    },

    /**
     * The permission will be added before the action is executed and removed afterwords
     */
    ADD_TEMP("~") {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            if (player.hasPermission(permission)) {
                action()
            } else {
                handler.add(player, permission)
                action()
                handler.remove(player, permission)
            }
        }
    };

    abstract fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit)

    companion object {

        private val VALUES = EnumSet.allOf(PermissionMode::class.java)

        fun getPermissionAndMode(permission: String): Pair<String, PermissionMode> {
            val prefix = permission.first().toString()

            return when (val mode = VALUES.firstOrNull { prefix == it.prefix } ?: REQUIRE) {
                NONE -> "" to mode
                REQUIRE -> permission to mode
                else -> permission.substring(1) to mode
            }
        }

    }

}