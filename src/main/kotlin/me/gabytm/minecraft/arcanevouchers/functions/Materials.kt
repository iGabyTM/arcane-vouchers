package me.gabytm.minecraft.arcanevouchers.functions

import me.gabytm.minecraft.arcanevouchers.ServerVersion
import org.bukkit.Material

fun Material.isPlayerHead(damage: Short): Boolean {
    return if (ServerVersion.IS_LEGACY) {
        name == "SKULL_ITEM" && damage == 3.toShort()
    } else {
        this == Material.PLAYER_HEAD
    }
}