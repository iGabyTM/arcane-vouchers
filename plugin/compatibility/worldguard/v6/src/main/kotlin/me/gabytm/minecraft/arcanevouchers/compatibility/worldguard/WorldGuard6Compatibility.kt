package me.gabytm.minecraft.arcanevouchers.compatibility.worldguard

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.ApplicableRegionSet
import org.bukkit.Location
import org.bukkit.entity.Player

class WorldGuard6Compatibility : WorldGuardCompatibility {

    private fun getRegionsAtLocation(location: Location): ApplicableRegionSet {
        return WorldGuardPlugin.inst()
            .regionContainer
            .createQuery()
            .getApplicableRegions(location)
    }

    /**
     * Gets the id of all regions from a certain [Location]
     * @return set of region ids
     */
    override fun getRegions(location: Location): Set<String> {
        return getRegionsAtLocation(location).map { it.id }.toSet()
    }

    override fun isOwner(location: Location, player: Player): Boolean {
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        return getRegionsAtLocation(location).any { it.isOwner(localPlayer) }
    }

    override fun isMember(location: Location, player: Player): Boolean {
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        return getRegionsAtLocation(location).any { it.isMember(localPlayer) }
    }

}