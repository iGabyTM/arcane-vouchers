package me.gabytm.minecraft.arcanevouchers.compat.worldguard

import org.bukkit.entity.Player

interface WorldGuardCompat {

    fun isWhitelisted(player: Player, regions: List<String>): Boolean

    fun isBlacklisted(player: Player, regions: List<String>): Boolean

}