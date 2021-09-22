package me.gabytm.minecraft.arcanevouchers.functions

import org.bukkit.permissions.Permissible

/**
 * Check if a [Permissible] has any permission from a list
 * @return whether the [Permissible] has any permission
 */
fun Permissible.any(vararg permissions: String): Boolean = permissions.any { hasPermission(it) }