package me.gabytm.minecraft.arcanevouchers.compat.worldguard

import org.bukkit.Location

interface WorldGuardCompat {

    fun getRegions(location: Location): Set<String>

}