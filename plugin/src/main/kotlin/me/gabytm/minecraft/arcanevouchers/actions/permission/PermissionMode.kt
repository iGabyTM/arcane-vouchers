package me.gabytm.minecraft.arcanevouchers.actions.permission

import me.gabytm.minecraft.arcanevouchers.Constant
import org.bukkit.entity.Player
import java.util.*

@Suppress("unused")
enum class PermissionMode {

    /**
     * No permission mode
     */
    NONE {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            action()
        }
    },

    /**
     * The permission is required for the action to run
     */
    REQUIRE {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            if (player.hasPermission(permission)) {
                action()
            }
        }
    },

    /**
     * The permission is required for the action to run, and it will be removed after the fist execution
     */
    REMOVE {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            if (player.hasPermission(permission)) {
                action()
                handler.remove(player, permission)
            }
        }
    },

    /**
     * The action will run only if the player doesn't have the permission
     */
    NO_PERMISSION {
        override fun execute(handler: PermissionHandler, player: Player, permission: String, action: () -> Unit) {
            if (!player.hasPermission(permission)) {
                action()
            }
        }
    },

    /**
     * The permission will be added after the first execution and the action won't run next time
     */
    ADD {
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
    ADD_TEMP {
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

        /**
         * Process an input string to a [Pair] of `<permission.node, PermissionMode>`
         * @param input input to process
         * @return [Pair]
         */
        fun getPermissionAndMode(input: String): Pair<String, PermissionMode> {
            // The input will be structured as 'MODE:permission.node'
            val parts = input.split(Constant.Separator.COLON)

            // If the input doesn't contain a semicolon, the default mode is REQUIRED
            if (parts.size == 1) {
                return input to REQUIRE
            }

            // Find the mode by its name or fallback to REQUIRE
            val mode = VALUES.firstOrNull { parts[0].equals(it.name, true) } ?: REQUIRE
            return parts[1] to mode
        }

    }

}