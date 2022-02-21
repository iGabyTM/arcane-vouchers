package me.gabytm.minecraft.arcanevouchers.compatibility.worldguard

import org.bukkit.Location

interface WorldGuardCompatibility {

    /**
     * Gets the id of all regions from a certain [Location]
     * @return set of region ids
     */
    fun getRegions(location: Location): Set<String>

}