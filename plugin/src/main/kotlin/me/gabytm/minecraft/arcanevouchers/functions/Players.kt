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
 * Set a [Player]'s item in hand using the right method for each game version
 * @param item item to set
 */
@Suppress("DEPRECATION")
fun Player.item(item: ItemStack?) {
    if (ServerVersion.HAS_OFF_HAND) {
        this.inventory.setItemInMainHand(item)
    } else {
        this.inventory.setItemInHand(item)
    }
}

/**
 * Add items to a player inventory and drop on the ground the leftovers
 * @param items items to add
 */
fun Player.giveItems(vararg items: ItemStack) {
    inventory.addItem(*items).values.forEach { world.dropItem(location, it) }
}

/**
 * Get the name of an [OfflinePlayer] and return a default value if it is null
 * @param default the default value returned in case the name is null
 * @return [OfflinePlayer.name] or `default` if it is null
 */
fun OfflinePlayer.name(default: String = ""): String = name ?: default