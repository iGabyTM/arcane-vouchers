package me.gabytm.minecraft.arcanevouchers.functions

import net.md_5.bungee.api.ChatColor

fun String.color(hex: Boolean = false): String {
    return ChatColor.translateAlternateColorCodes('&', this)
}