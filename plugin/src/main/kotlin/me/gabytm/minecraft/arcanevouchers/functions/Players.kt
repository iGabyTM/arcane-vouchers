package me.gabytm.minecraft.arcanevouchers.functions

import me.gabytm.minecraft.arcanevouchers.ServerVersion
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Suppress("DEPRECATION")
fun Player.item(): ItemStack {
    return if (ServerVersion.HAS_OFF_HAND) {
        this.inventory.itemInMainHand
    } else {
        this.inventory.itemInHand
    }
}