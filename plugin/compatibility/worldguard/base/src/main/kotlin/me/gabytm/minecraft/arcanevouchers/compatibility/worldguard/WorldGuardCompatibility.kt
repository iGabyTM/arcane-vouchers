package me.gabytm.minecraft.arcanevouchers.compatibility.worldguard

import org.bukkit.Location
import org.bukkit.entity.Player

interface WorldGuardCompatibility {

    /**
     * Gets the id of all regions from a certain [Location]
     * @return set of region ids
     */
    fun getRegions(location: Location): Set<String>

    /**
     * Checks if the player is the owner of any regions at the given [location]
     * @return whether the player is the owner of a region
     */
    fun isOwner(location: Location, player: Player): Boolean

    /**
     * Checks if the player is a member of any regions at the given [location]
     * @return whether the player is a member of a region
     */
    fun isMember(location: Location, player: Player): Boolean

}