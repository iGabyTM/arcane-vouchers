package me.gabytm.minecraft.arcanevouchers.functions

import me.gabytm.minecraft.arcanevouchers.ServerVersion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Get a [Player]'s item held using the right method for each game version
 * @return the [ItemStack] that the player is holding
 */
@Suppress("DEPRECATION")
fun Player.item(): ItemStack {
    return if (ServerVersion.HAS_OFF_HAND) {
        this.inventory.itemInMainHand
    } else {
        this.inventory.itemInHand
    }
}

/**
 * Get the name of an [OfflinePlayer] and return a default value if it is null
 * @param default the default value returned in case the name is null
 * @return [OfflinePlayer.name] or `default` if it is null
 */
fun OfflinePlayer.name(default: String = ""): String = name ?: default